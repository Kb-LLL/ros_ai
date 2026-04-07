package com.itheima.consultant.service;

import cn.dev33.satoken.stp.StpUtil;
import com.itheima.consultant.entity.Conversation;
import com.itheima.consultant.entity.Message;
import com.itheima.consultant.mapper.ConversationMapper;
import com.itheima.consultant.mapper.MessageMapper;
import com.itheima.consultant.repository.RedisChatMemoryStore;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ConversationService {

    @Autowired
    private ConversationMapper conversationMapper;
    @Autowired
    private MessageMapper messageMapper;
    @Autowired
    private RedisChatMemoryStore redisChatMemoryStore;

    private Long currentUserId() {
        return Long.valueOf(StpUtil.getLoginId().toString());
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

    /** 获取会话消息（校验归属） */
    public List<Message> getMessages(Long conversationId) {
        Conversation conv = conversationMapper.findById(conversationId);
        if (conv == null || !conv.getUserId().equals(currentUserId())) {
            throw new RuntimeException("会话不存在");
        }
        return messageMapper.findByConversationId(conversationId);
    }

    /** 保存消息，首条用户消息自动设为标题 */
    public void saveMessage(Long conversationId, String role, String content) {
        Message msg = new Message();
        msg.setConversationId(conversationId);
        msg.setRole(role);
        msg.setContent(content);
        messageMapper.insert(msg);
        conversationMapper.touch(conversationId);

        // 首条用户消息 → 自动截取前20字作为标题
        if ("user".equals(role)) {
            Conversation conv = conversationMapper.findById(conversationId);
            if (conv != null && "新对话".equals(conv.getTitle())) {
                String title = content.length() > 20
                        ? content.substring(0, 20) + "..."
                        : content;
                conversationMapper.updateTitle(conversationId, title);
            }
        }
    }

    /** 重命名 */
    public void rename(Long conversationId, String title) {
        Conversation conv = conversationMapper.findById(conversationId);
        if (conv == null || !conv.getUserId().equals(currentUserId())) {
            throw new RuntimeException("会话不存在");
        }
        conversationMapper.updateTitle(conversationId, title);
    }

    /** 删除会话及消息 */
    @Transactional
    public void delete(Long conversationId) {
        messageMapper.deleteByConversationId(conversationId);
        conversationMapper.deleteByIdAndUserId(conversationId, currentUserId());
    }

    /** 删除最后一条 AI 消息（用于重新生成） */
    @Transactional
    public void deleteLastAiMessage(String memoryId) {
        List<ChatMessage> messages = redisChatMemoryStore.getMessages(memoryId);
        if (messages != null && !messages.isEmpty()) {
            ChatMessage last = messages.get(messages.size() - 1);
            if (last instanceof AiMessage) {
                messages.remove(messages.size() - 1);
                redisChatMemoryStore.updateMessages(memoryId, messages);
            }
        }
    }
}