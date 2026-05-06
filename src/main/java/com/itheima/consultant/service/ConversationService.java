package com.itheima.consultant.service;

import cn.dev33.satoken.stp.StpUtil;
import com.itheima.consultant.entity.Conversation;
import com.itheima.consultant.entity.Message;
import com.itheima.consultant.entity.UserDocument;
import com.itheima.consultant.mapper.ConversationDocumentBindingMapper;
import com.itheima.consultant.mapper.ConversationMapper;
import com.itheima.consultant.mapper.MessageMapper;
import com.itheima.consultant.mapper.UserDocumentMapper;
import com.itheima.consultant.repository.RedisChatMemoryStore;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ConversationService {

    @Autowired
    private ConversationMapper conversationMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private UserDocumentMapper userDocumentMapper;

    @Autowired
    private ConversationDocumentBindingMapper conversationDocumentBindingMapper;

    @Autowired
    private RedisChatMemoryStore redisChatMemoryStore;

    private Long currentUserId() {
        return Long.valueOf(StpUtil.getLoginId().toString());
    }

    private Conversation requireOwnedConversation(Long conversationId) {
        Conversation conv = conversationMapper.findById(conversationId);
        if (conv == null || !conv.getUserId().equals(currentUserId())) {
            throw new RuntimeException("会话不存在");
        }
        return conv;
    }

    /** 创建新会话 */
    public Long createConversation() {
        Conversation conv = new Conversation();
        conv.setUserId(currentUserId());
        conv.setTitle("新对话");
        conversationMapper.insert(conv);
        return conv.getId();
    }

    /** 当前用户的会话列表 */
    public List<Conversation> listConversations() {
        return conversationMapper.findByUserId(currentUserId());
    }

    /** 获取会话消息并校验归属 */
    public List<Message> getMessages(Long conversationId) {
        requireOwnedConversation(conversationId);
        return messageMapper.findByConversationId(conversationId);
    }

    /** 保存消息，首条用户消息自动设置标题 */
    public void saveMessage(Long conversationId, String role, String content) {
        Conversation conversation = requireOwnedConversation(conversationId);

        Message msg = new Message();
        msg.setConversationId(conversationId);
        msg.setRole(role);
        msg.setContent(content);
        messageMapper.insert(msg);
        conversationMapper.touch(conversationId);

        if ("user".equals(role)) {
            if ("新对话".equals(conversation.getTitle())) {
                String title = content.length() > 20
                        ? content.substring(0, 20) + "..."
                        : content;
                conversationMapper.updateTitle(conversationId, title);
            }
        }
    }

    /** 重命名会话 */
    public void rename(Long conversationId, String title) {
        requireOwnedConversation(conversationId);
        conversationMapper.updateTitle(conversationId, title);
    }

    /** 获取当前会话绑定的文件 */
    public List<UserDocument> listBoundDocuments(Long conversationId) {
        requireOwnedConversation(conversationId);
        Long userId = currentUserId();
        List<UserDocument> documents = conversationDocumentBindingMapper.findDocumentsByConversationId(conversationId);
        documents.forEach(document -> {
            document.setObjectKey(buildKnowledgeObjectKey(userId, document));
            document.setStorageType("minio");
        });
        return documents;
    }

    /** 获取当前会话绑定的文件 ID */
    public List<Long> listBoundDocumentIds(Long conversationId) {
        requireOwnedConversation(conversationId);
        return conversationDocumentBindingMapper.findDocumentIdsByConversationId(conversationId);
    }

    /** 更新会话绑定文件 */
    @Transactional
    public void bindDocuments(Long conversationId, List<Long> documentIds) {
        requireOwnedConversation(conversationId);
        List<Long> safeDocumentIds = documentIds == null ? List.of() : documentIds.stream()
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();

        Map<Long, UserDocument> userDocuments = userDocumentMapper.findByUserId(currentUserId()).stream()
                .collect(Collectors.toMap(UserDocument::getId, Function.identity()));

        for (Long documentId : safeDocumentIds) {
            if (!userDocuments.containsKey(documentId)) {
                throw new RuntimeException("存在无权限访问的文件");
            }
        }

        conversationDocumentBindingMapper.deleteByConversationId(conversationId);
        for (Long documentId : safeDocumentIds) {
            conversationDocumentBindingMapper.insert(conversationId, documentId);
        }
        conversationMapper.touch(conversationId);
    }

    /** 删除会话及消息 */
    @Transactional
    public void delete(Long conversationId) {
        requireOwnedConversation(conversationId);
        conversationDocumentBindingMapper.deleteByConversationId(conversationId);
        messageMapper.deleteByConversationId(conversationId);
        conversationMapper.deleteByIdAndUserId(conversationId, currentUserId());
    }

    /** 删除最后一条 AI 消息，用于重新生成 */
    @Transactional
    public void deleteLastAiMessage(Long conversationId) {
        requireOwnedConversation(conversationId);

        Message lastAssistantMessage = messageMapper.findLastAssistantMessage(conversationId);
        if (lastAssistantMessage != null) {
            messageMapper.deleteById(lastAssistantMessage.getId());
            conversationMapper.touch(conversationId);
        }

        List<ChatMessage> messages = redisChatMemoryStore.getMessages(conversationId.toString());
        if (messages != null && !messages.isEmpty()) {
            ChatMessage last = messages.get(messages.size() - 1);
            if (last instanceof AiMessage) {
                messages.remove(messages.size() - 1);
                redisChatMemoryStore.updateMessages(conversationId.toString(), messages);
            }
        }
    }

    private String buildKnowledgeObjectKey(Long userId, UserDocument document) {
        return "knowledge/" + userId + "/" + document.getId() + "/" + sanitizeFileName(document.getFileName());
    }

    private String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "unknown";
        }
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
