package com.itheima.consultant.service;

import cn.dev33.satoken.stp.StpUtil;
import com.itheima.consultant.entity.UserDocument;
import com.itheima.consultant.mapper.UserDocumentMapper;
import dev.langchain4j.community.store.embedding.redis.RedisEmbeddingStore;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class DocumentService {

    @Autowired
    private UserDocumentMapper documentMapper;

    @Autowired
    private RedisEmbeddingStore redisEmbeddingStore;

    @Autowired(required = false)
    private EmbeddingModel embeddingModel;

    private Long currentUserId() {
        return Long.valueOf(StpUtil.getLoginId().toString());
    }

    /** 上传并解析文件，写入向量库 */
    public UserDocument uploadDocument(MultipartFile file) throws IOException {
        Long userId = currentUserId();
        String originalName = file.getOriginalFilename();
        String ext = originalName != null
                ? originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase()
                : "unknown";

        // 1. 提取文本
        String text = extractText(file, ext);
        if (text == null || text.isBlank()) {
            throw new RuntimeException("文件内容为空或格式不支持");
        }

        // 2. 写入向量库（携带 userId 元数据，实现数据隔离）
        Document document = Document.from(text, Metadata.from("userId", userId.toString())
                .put("fileName", originalName));

        DocumentSplitter splitter = DocumentSplitters.recursive(500, 100);

        if (embeddingModel != null) {
            EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                    .embeddingStore(redisEmbeddingStore)
                    .embeddingModel(embeddingModel)
                    .documentSplitter(splitter)
                    .build();
            ingestor.ingest(document);
        }

        // 3. 记录到数据库
        UserDocument doc = new UserDocument();
        doc.setUserId(userId);
        doc.setFileName(originalName);
        doc.setFileType(ext);
        doc.setFileSize(file.getSize());
        documentMapper.insert(doc);

        return doc;
    }

    /** 获取当前用户文件列表 */
    public List<UserDocument> listDocuments() {
        return documentMapper.findByUserId(currentUserId());
    }

    /** 删除文件 */
    public void deleteDocument(Long id) {
        documentMapper.softDelete(id, currentUserId());
    }

    /** 提取文本（支持 PDF / TXT，可按需扩展） */
    private String extractText(MultipartFile file, String ext) throws IOException {
        return switch (ext) {
            case "pdf" -> {
                try (PDDocument pdf = PDDocument.load(file.getInputStream())) {
                    yield new PDFTextStripper().getText(pdf);
                }
            }
            case "txt", "md" -> new String(file.getBytes());
            default -> throw new RuntimeException("暂不支持 ." + ext + " 格式，请上传 PDF/TXT/MD");
        };
    }
}