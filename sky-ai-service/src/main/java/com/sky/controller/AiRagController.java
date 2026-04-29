package com.sky.controller;

import com.sky.service.AiRagChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * RAG 对话控制器
 *
 * 与普通对话的区别：
 * - /api/ai/chat：普通对话，LLM 仅凭自身知识回答
 * - /api/ai/rag-chat：RAG 对话，LLM 根据知识库文档回答
 *
 * 测试方法：
 * 1. 启动服务
 * 2. POST http://localhost:8080/api/ai/rag-chat
 * 3. Body: "黄焖鸡米饭多少钱？"
 * 4. 对比 /api/ai/chat 的回答，观察 RAG 的效果
 */
@RestController
@RequestMapping("/api/ai")
public class AiRagController {

    @Autowired
    private AiRagChatService aiRagChatService;

    @PostMapping("/rag-chat")
    public Flux<String> ragChat(@RequestBody String message) {
        String sessionId = "user-1";
        return aiRagChatService.chat(message, sessionId);
    }
}
