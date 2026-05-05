package com.sky.controller;



import com.sky.service.AiChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Map;

/*
*
* 智能客服
* */
@RestController
@RequestMapping("/api/ai")
public class AiController {
    @Autowired
    private AiChatService aiChatService;

    @PostMapping("/chat")
    public Flux<String> chat(@RequestBody String message) {

        String sessionId = "user-1";
        return aiChatService.chat(message, sessionId);
    }
}
