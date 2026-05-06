package com.itheima.consultant.service;

import cn.dev33.satoken.stp.StpUtil;
import com.itheima.consultant.entity.Message;
import dev.langchain4j.community.store.embedding.redis.RedisEmbeddingStore;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.image.Image;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.filter.MetadataFilterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RagChatService {

    private static final int HISTORY_MESSAGE_LIMIT = 12;
    private static final int MAX_RAG_RESULTS = 4;
    private static final int SCOPED_RAG_RESULTS = 16;
    private static final double MIN_RAG_SCORE = 0.55D;

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private RedisEmbeddingStore redisEmbeddingStore;

    @Autowired(required = false)
    private EmbeddingModel embeddingModel;

    @Autowired
    @Qualifier("openAiStreamingChatModel")
    private OpenAiStreamingChatModel streamingChatModel;

    @Autowired
    @Qualifier("visionStreamingChatModel")
    private OpenAiStreamingChatModel visionStreamingChatModel;

    @Autowired
    private MinioStorageService minioStorageService;

    public Flux<String> chat(Long conversationId, String message) {
        return chat(conversationId, message, List.of());
    }

    public Flux<String> chat(Long conversationId, String message, List<String> imageKeys) {
        String question = message == null ? "" : message.trim();
        List<String> safeImageKeys = imageKeys == null ? List.of() : imageKeys;
        if (question.isBlank() && safeImageKeys.isEmpty()) {
            return Flux.error(new IllegalArgumentException("Message must not be empty"));
        }
        if (embeddingModel == null) {
            return Flux.error(new IllegalStateException("Embedding model is not configured"));
        }
        if (question.isBlank()) {
            question = "Please analyze the uploaded image";
        }

        boolean hasImages = !safeImageKeys.isEmpty();
        List<ChatMessage> promptMessages = buildPromptMessages(conversationId, question, safeImageKeys);
        OpenAiStreamingChatModel activeModel = hasImages ? visionStreamingChatModel : streamingChatModel;
        return Flux.create(sink -> {
            try {
                activeModel.chat(promptMessages, new StreamingChatResponseHandler() {
                    @Override
                    public void onPartialResponse(String partialResponse) {
                        sink.next(partialResponse);
                    }

                    @Override
                    public void onCompleteResponse(dev.langchain4j.model.chat.response.ChatResponse response) {
                        sink.complete();
                    }

                    @Override
                    public void onError(Throwable error) {
                        sink.error(error);
                    }
                });
            } catch (Exception exception) {
                sink.error(exception);
            }
        });
    }

    private List<ChatMessage> buildPromptMessages(Long conversationId, String question, List<String> imageKeys) {
        List<Message> history = conversationService.getMessages(conversationId);
        List<TextSegment> segments = retrieveSegments(conversationId, question);
        boolean hasSpreadsheet = segments.stream()
                .map(TextSegment::metadata)
                .filter(Objects::nonNull)
                .map(metadata -> metadata.getString("fileType"))
                .anyMatch(this::isSpreadsheetFileType);

        List<ChatMessage> promptMessages = new ArrayList<>();
        promptMessages.add(SystemMessage.from(buildSystemPrompt(segments, !imageKeys.isEmpty(), hasSpreadsheet)));
        promptMessages.addAll(toConversationHistoryMessages(history));
        promptMessages.add(buildCurrentUserMessage(question, imageKeys));
        return promptMessages;
    }

    private List<TextSegment> retrieveSegments(Long conversationId, String question) {
        Long userId = currentUserId();
        List<Long> boundDocumentIds = conversationService.listBoundDocumentIds(conversationId);
        Set<String> scopedDocumentIds = boundDocumentIds.stream()
                .map(String::valueOf)
                .collect(Collectors.toCollection(HashSet::new));
        Embedding queryEmbedding = embeddingModel.embed(question).content();
        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(boundDocumentIds.isEmpty() ? MAX_RAG_RESULTS : SCOPED_RAG_RESULTS)
                .minScore(MIN_RAG_SCORE)
                .filter(MetadataFilterBuilder.metadataKey("userId").isEqualTo(userId.toString()))
                .build();

        EmbeddingSearchResult<TextSegment> result = redisEmbeddingStore.search(request);
        return result.matches().stream()
                .map(EmbeddingMatch::embedded)
                .filter(Objects::nonNull)
                .filter(segment -> scopedDocumentIds.isEmpty() || matchesBoundDocuments(segment, scopedDocumentIds))
                .limit(MAX_RAG_RESULTS)
                .collect(Collectors.toList());
    }

    private List<ChatMessage> toConversationHistoryMessages(List<Message> history) {
        List<Message> recentHistory;
        if (history.size() > HISTORY_MESSAGE_LIMIT) {
            recentHistory = history.subList(history.size() - HISTORY_MESSAGE_LIMIT, history.size());
        } else {
            recentHistory = history;
        }

        List<ChatMessage> messages = new ArrayList<>();
        for (Message historyMessage : recentHistory) {
            if ("assistant".equals(historyMessage.getRole())) {
                messages.add(AiMessage.from(historyMessage.getContent()));
            } else if ("user".equals(historyMessage.getRole())) {
                messages.add(UserMessage.from(historyMessage.getContent()));
            }
        }
        return messages;
    }

    private ChatMessage buildCurrentUserMessage(String question, List<String> imageKeys) {
        if (imageKeys.isEmpty()) {
            return UserMessage.from(question);
        }

        List<Content> contents = new ArrayList<>();
        contents.add(TextContent.from(question));
        for (String imageKey : imageKeys) {
            contents.add(loadImageContent(imageKey));
        }
        return UserMessage.from(contents);
    }

    private ImageContent loadImageContent(String objectKey) {
        Long userId = currentUserId();
        if (!minioStorageService.isUserChatImage(objectKey, userId)) {
            throw new RuntimeException("Image asset does not belong to current user");
        }

        MinioStorageService.StoredObject storedObject = minioStorageService.readObject(objectKey);
        String base64Data = java.util.Base64.getEncoder().encodeToString(storedObject.bytes());
        Image image = Image.builder()
                .base64Data(base64Data)
                .mimeType(storedObject.contentType())
                .build();
        return ImageContent.from(image);
    }

    private String buildSystemPrompt(List<TextSegment> segments, boolean hasImages, boolean hasSpreadsheet) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a document analysis assistant. ");
        prompt.append("Prefer the uploaded document context when it is relevant. ");
        prompt.append("If the context is insufficient, say so clearly before giving a best-effort answer. ");
        prompt.append("Answer in Chinese and keep the answer grounded in the retrieved content.");
        if (hasImages) {
            prompt.append(" When images are provided, analyze the visual details carefully before answering.");
        }
        if (hasSpreadsheet) {
            prompt.append(" When spreadsheet content is relevant, answer with clear sections, key metrics, and markdown tables when helpful.");
        }

        if (segments.isEmpty()) {
            prompt.append("\n\nNo relevant document snippets were retrieved for this turn.");
            return prompt.toString();
        }

        prompt.append("\n\nRetrieved document snippets:\n");
        for (int index = 0; index < segments.size(); index++) {
            TextSegment segment = segments.get(index);
            String fileName = segment.metadata() == null ? null : segment.metadata().getString("fileName");
            if (fileName == null || fileName.isBlank()) {
                fileName = "unknown";
            }

            prompt.append("[")
                    .append(index + 1)
                    .append("] file=")
                    .append(fileName)
                    .append("\n")
                    .append(segment.text())
                    .append("\n\n");
        }
        return prompt.toString();
    }

    private Long currentUserId() {
        return Long.valueOf(StpUtil.getLoginId().toString());
    }

    private boolean matchesBoundDocuments(TextSegment segment, Set<String> scopedDocumentIds) {
        if (segment.metadata() == null) {
            return false;
        }
        String documentId = segment.metadata().getString("documentId");
        return documentId != null && scopedDocumentIds.contains(documentId);
    }

    private boolean isSpreadsheetFileType(String fileType) {
        return "xls".equalsIgnoreCase(fileType) || "xlsx".equalsIgnoreCase(fileType);
    }
}
