package com.itheima.consultant.service;

import cn.dev33.satoken.stp.StpUtil;
import com.itheima.consultant.entity.UserDocument;
import com.itheima.consultant.mapper.ConversationDocumentBindingMapper;
import com.itheima.consultant.mapper.UserDocumentMapper;
import dev.langchain4j.community.store.embedding.redis.RedisEmbeddingStore;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.SearchResult;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class DocumentPipelineService {

    private static final Logger log = LoggerFactory.getLogger(DocumentPipelineService.class);
    private static final long MAX_FILE_SIZE = 20L * 1024 * 1024;
    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of("pdf", "doc", "docx", "txt", "md", "xls", "xlsx");
    private static final DocumentSplitter SPLITTER = DocumentSplitters.recursive(500, 100);
    private static final int REDIS_DELETE_BATCH_SIZE = 500;
    private static final int MAX_SHEETS = 10;
    private static final int MAX_ROWS_PER_SHEET = 200;
    private static final int MAX_COLUMNS = 20;
    private static final int MAX_CELL_LENGTH = 120;

    @Autowired
    private UserDocumentMapper documentMapper;

    @Autowired
    private ConversationDocumentBindingMapper conversationDocumentBindingMapper;

    @Autowired
    private RedisEmbeddingStore redisEmbeddingStore;

    @Autowired
    private MinioStorageService minioStorageService;

    @Autowired(required = false)
    private EmbeddingModel embeddingModel;

    @Value("${langchain4j.community.redis.host:localhost}")
    private String redisHost;

    @Value("${langchain4j.community.redis.port:6379}")
    private int redisPort;

    @Value("${langchain4j.community.redis.index-name:embedding-index}")
    private String redisIndexName;

    private final ApacheTikaDocumentParser tikaDocumentParser = new ApacheTikaDocumentParser();

    public UserDocument uploadDocument(MultipartFile file) throws IOException {
        validateFile(file);

        Long userId = currentUserId();
        String fileName = originalFileName(file);
        String extension = extensionOf(fileName);

        UserDocument documentRecord = new UserDocument();
        documentRecord.setUserId(userId);
        documentRecord.setFileName(fileName);
        documentRecord.setFileType(extension);
        documentRecord.setFileSize(file.getSize());
        documentMapper.insert(documentRecord);

        String objectKey = minioStorageService.uploadKnowledgeFile(file, userId, documentRecord.getId(), fileName);
        documentRecord.setObjectKey(objectKey);
        documentRecord.setStorageType("minio");

        try {
            Document document = parseDocument(file, documentRecord);
            ingest(document);
            return documentRecord;
        } catch (Exception exception) {
            minioStorageService.deleteObjectQuietly(objectKey);
            documentMapper.hardDeleteByIdAndUserId(documentRecord.getId(), userId);
            if (exception instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new RuntimeException("Failed to parse and ingest file", exception);
        }
    }

    public List<UserDocument> listDocuments() {
        Long userId = currentUserId();
        List<UserDocument> documents = documentMapper.findByUserId(userId);
        documents.forEach(document -> {
            document.setObjectKey(minioStorageService.buildKnowledgeObjectKey(
                    userId, document.getId(), document.getFileName()));
            document.setStorageType("minio");
        });
        return documents;
    }

    @Transactional
    public void deleteDocument(Long id) {
        Long userId = currentUserId();
        UserDocument document = documentMapper.findByIdAndUserId(id, userId);
        if (document == null || (document.getStatus() != null && document.getStatus() == 0)) {
            throw new RuntimeException("Document not found");
        }

        removeDocumentEmbeddings(userId, id);
        minioStorageService.deleteObjectQuietly(
                minioStorageService.buildKnowledgeObjectKey(userId, id, document.getFileName()));
        conversationDocumentBindingMapper.deleteByDocumentId(id);
        documentMapper.softDelete(id, userId);
    }

    private void removeDocumentEmbeddings(Long userId, Long documentId) {
        String queryText = String.format("(@userId:{%s} @documentId:{%s})",
                escapeTagValue(userId.toString()),
                escapeTagValue(documentId.toString()));

        int removed = 0;
        try (JedisPooled client = new JedisPooled(redisHost, redisPort)) {
            while (true) {
                Query query = new Query(queryText)
                        .limit(0, REDIS_DELETE_BATCH_SIZE)
                        .dialect(2);
                SearchResult result = client.ftSearch(redisIndexName, query);
                List<String> keys = result.getDocuments().stream()
                        .map(redis.clients.jedis.search.Document::getId)
                        .filter(key -> key != null && !key.isBlank())
                        .toList();

                if (keys.isEmpty()) {
                    break;
                }

                client.del(keys.toArray(String[]::new));
                removed += keys.size();
            }
        } catch (JedisDataException exception) {
            if (isMissingIndexError(exception)) {
                log.warn("Redis vector index {} does not exist while deleting document {}", redisIndexName, documentId);
                return;
            }
            throw exception;
        }

        if (removed > 0) {
            log.info("Deleted {} embedding chunks for document {}", removed, documentId);
        }
    }

    private boolean isMissingIndexError(JedisDataException exception) {
        String message = exception.getMessage();
        return message != null && message.toLowerCase().contains("no such index");
    }

    private String escapeTagValue(String value) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (Character.isLetterOrDigit(c) || c == '_') {
                builder.append(c);
            } else {
                builder.append('\\').append(c);
            }
        }
        return builder.toString();
    }

    private void ingest(Document document) {
        if (embeddingModel == null) {
            throw new RuntimeException("Embedding model is not configured");
        }

        EmbeddingStoreIngestor.builder()
                .embeddingStore(redisEmbeddingStore)
                .embeddingModel(embeddingModel)
                .documentSplitter(SPLITTER)
                .build()
                .ingest(document);
    }

    private Document parseDocument(MultipartFile file, UserDocument documentRecord) throws IOException {
        if (isSpreadsheet(documentRecord.getFileType())) {
            return parseSpreadsheet(file, documentRecord);
        }

        try (InputStream inputStream = file.getInputStream()) {
            Document parsed = tikaDocumentParser.parse(inputStream);
            String text = normalizeText(parsed.text());
            if (text.isBlank()) {
                throw new RuntimeException("No readable text was extracted from the file");
            }

            Metadata metadata = new Metadata();
            metadata.put("userId", documentRecord.getUserId().toString());
            metadata.put("documentId", documentRecord.getId().toString());
            metadata.put("fileName", documentRecord.getFileName());
            metadata.put("fileType", documentRecord.getFileType());

            if (parsed.metadata() != null) {
                metadata = metadata.merge(parsed.metadata());
                metadata.put("userId", documentRecord.getUserId().toString());
                metadata.put("documentId", documentRecord.getId().toString());
                metadata.put("fileName", documentRecord.getFileName());
                metadata.put("fileType", documentRecord.getFileType());
            }

            return Document.from(text, metadata);
        }
    }

    private Document parseSpreadsheet(MultipartFile file, UserDocument documentRecord) throws IOException {
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {
            String text = buildSpreadsheetText(workbook, documentRecord.getFileName());
            if (text.isBlank()) {
                throw new RuntimeException("No readable data was extracted from the spreadsheet");
            }

            Metadata metadata = new Metadata();
            metadata.put("userId", documentRecord.getUserId().toString());
            metadata.put("documentId", documentRecord.getId().toString());
            metadata.put("fileName", documentRecord.getFileName());
            metadata.put("fileType", documentRecord.getFileType());
            metadata.put("sourceType", "spreadsheet");
            return Document.from(text, metadata);
        } catch (Exception exception) {
            if (exception instanceof IOException ioException) {
                throw ioException;
            }
            throw new RuntimeException("Failed to parse spreadsheet", exception);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File must not be empty");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("File size must be less than 20MB");
        }

        String extension = extensionOf(originalFileName(file));
        if (!SUPPORTED_EXTENSIONS.contains(extension)) {
            throw new RuntimeException("Only PDF, Word, Excel, TXT and MD files are supported");
        }
    }

    private boolean isSpreadsheet(String fileType) {
        return "xls".equals(fileType) || "xlsx".equals(fileType);
    }

    private String buildSpreadsheetText(Workbook workbook, String fileName) {
        StringBuilder builder = new StringBuilder();
        builder.append("文件名: ").append(fileName).append('\n');
        builder.append("文件类型: Excel").append('\n').append('\n');

        DataFormatter formatter = new DataFormatter();
        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        int sheetCount = Math.min(workbook.getNumberOfSheets(), MAX_SHEETS);

        for (int sheetIndex = 0; sheetIndex < sheetCount; sheetIndex++) {
            Sheet sheet = workbook.getSheetAt(sheetIndex);
            List<List<String>> rows = extractSheetRows(sheet, formatter, evaluator);
            if (rows.isEmpty()) {
                continue;
            }

            builder.append("## 工作表: ").append(sheet.getSheetName()).append('\n');
            appendSheetTable(builder, rows);
            builder.append('\n');
        }

        return normalizeText(builder.toString());
    }

    private List<List<String>> extractSheetRows(Sheet sheet, DataFormatter formatter, FormulaEvaluator evaluator) {
        List<List<String>> rows = new ArrayList<>();
        int collectedRows = 0;

        for (int rowIndex = sheet.getFirstRowNum(); rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }

            List<String> values = new ArrayList<>();
            int lastCellNum = Math.min(Math.max(row.getLastCellNum(), 0), MAX_COLUMNS);
            for (int cellIndex = 0; cellIndex < lastCellNum; cellIndex++) {
                Cell cell = row.getCell(cellIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                String value = cell == null ? "" : normalizeCellValue(formatter.formatCellValue(cell, evaluator));
                values.add(value);
            }

            if (values.stream().allMatch(String::isBlank)) {
                continue;
            }

            rows.add(values);
            collectedRows++;
            if (collectedRows >= MAX_ROWS_PER_SHEET) {
                break;
            }
        }
        return rows;
    }

    private void appendSheetTable(StringBuilder builder, List<List<String>> rows) {
        List<String> header = normalizeHeader(rows.get(0));
        builder.append('|');
        for (String column : header) {
            builder.append(' ').append(column).append(" |");
        }
        builder.append('\n').append('|');
        for (int i = 0; i < header.size(); i++) {
            builder.append(" --- |");
        }
        builder.append('\n');

        if (rows.size() == 1) {
            return;
        }

        for (int rowIndex = 1; rowIndex < rows.size(); rowIndex++) {
            List<String> row = rows.get(rowIndex);
            builder.append('|');
            for (int columnIndex = 0; columnIndex < header.size(); columnIndex++) {
                String value = columnIndex < row.size() ? row.get(columnIndex) : "";
                builder.append(' ').append(value).append(" |");
            }
            builder.append('\n');
        }
        if (rows.size() >= MAX_ROWS_PER_SHEET) {
            builder.append('\n').append("注: 为了检索效率，仅保留前 ")
                    .append(MAX_ROWS_PER_SHEET)
                    .append(" 行数据。").append('\n');
        }
    }

    private List<String> normalizeHeader(List<String> headerRow) {
        List<String> header = new ArrayList<>();
        for (int index = 0; index < Math.min(headerRow.size(), MAX_COLUMNS); index++) {
            String cell = normalizeCellValue(headerRow.get(index));
            header.add(cell.isBlank() ? "列" + (index + 1) : cell);
        }
        return header;
    }

    private String normalizeCellValue(String value) {
        String text = normalizeText(value).replace('\n', ' ').replace('\r', ' ');
        if (text.length() > MAX_CELL_LENGTH) {
            return text.substring(0, MAX_CELL_LENGTH) + "...";
        }
        return text;
    }

    private String normalizeText(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\u0000", "").trim();
    }

    private String originalFileName(MultipartFile file) {
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            throw new RuntimeException("File name is missing");
        }
        return originalName;
    }

    private String extensionOf(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index < 0 || index == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(index + 1).toLowerCase();
    }

    private Long currentUserId() {
        return Long.valueOf(StpUtil.getLoginId().toString());
    }
}
