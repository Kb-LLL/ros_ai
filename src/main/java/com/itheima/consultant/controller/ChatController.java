package com.itheima.consultant.controller;

import com.itheima.consultant.dto.ChatStreamRequest;
import com.itheima.consultant.service.RagChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class ChatController {
    @Autowired
    private RagChatService ragChatService;
//    @RequestMapping(value = "/chat"/* ,produces = "text/html;charset=utf-8" */)
    @GetMapping(value = "/chat", produces = MediaType.TEXT_PLAIN_VALUE)
    public Flux<String> chat(@RequestParam("memoryId") Long memoryId,
                             @RequestParam("message") String message) {
        return ragChatService.chat(memoryId, message);
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_PLAIN_VALUE)
    public Flux<String> chatStream(@RequestBody ChatStreamRequest request) {
        return ragChatService.chat(request.getMemoryId(), request.getMessage(), request.getImageKeys());
    }

    /*@RequestMapping("/chat")
    public String chat(String message){
        String result = consultantService.chat(message);
        return result;
    }*/

    /*@Autowired
    private OpenAiChatModel model;
    @RequestMapping("/chat")
    public String chat(String message){//浏览器传递的用户问题
        String result = model.chat(message);
        return result;
    }*/
}
