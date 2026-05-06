package com.itheima.consultant.controller;

import com.itheima.consultant.common.Result;
import com.itheima.consultant.dto.ConversationDocumentSelectionRequest;
import com.itheima.consultant.entity.Conversation;
import com.itheima.consultant.entity.Message;
import com.itheima.consultant.entity.UserDocument;
import com.itheima.consultant.service.ConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/conversation")
public class ConversationController {

    @Autowired
    private ConversationService conversationService;

    @PostMapping("/create")
    public Result<Long> create() {
        return Result.success(conversationService.createConversation());
    }

    @GetMapping("/list")
    public Result<List<Conversation>> list() {
        return Result.success(conversationService.listConversations());
    }

    @GetMapping("/{id}/messages")
    public Result<List<Message>> messages(@PathVariable Long id) {
        return Result.success(conversationService.getMessages(id));
    }

    @PostMapping("/{id}/message")
    public Result<?> saveMessage(@PathVariable Long id,
                                 @RequestBody Map<String, String> body) {
        conversationService.saveMessage(id, body.get("role"), body.get("content"));
        return Result.success();
    }

    @PutMapping("/{id}/rename")
    public Result<?> rename(@PathVariable Long id,
                            @RequestBody Map<String, String> body) {
        conversationService.rename(id, body.get("title"));
        return Result.success();
    }

    @GetMapping("/{id}/documents")
    public Result<List<UserDocument>> listBoundDocuments(@PathVariable Long id) {
        return Result.success(conversationService.listBoundDocuments(id));
    }

    @PutMapping("/{id}/documents")
    public Result<?> bindDocuments(@PathVariable Long id,
                                   @RequestBody ConversationDocumentSelectionRequest request) {
        conversationService.bindDocuments(id, request.getDocumentIds());
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        conversationService.delete(id);
        return Result.success();
    }

    @DeleteMapping("/{id}/last-ai-message")
    public Result<?> deleteLastAiMessage(@PathVariable Long id) {
        conversationService.deleteLastAiMessage(id);
        return Result.success();
    }
}
