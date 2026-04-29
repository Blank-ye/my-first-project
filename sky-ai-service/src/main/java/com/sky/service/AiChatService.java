package com.sky.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;
import reactor.core.publisher.Flux;

@AiService(
        wiringMode = AiServiceWiringMode.EXPLICIT,// 显式注入模型
        chatModel = "openAiChatModel",// 绑定普通模型
        streamingChatModel = "openAiStreamingChatModel",// 绑定流式模型
        chatMemoryProvider = "getMyChatMemoryProvider"// 配置带有自定义的 ChatMemoryStore类的会话记忆提供者
)
public interface AiChatService {

        @SystemMessage("""
                你是小仓，是一位有三年经验的客服。请遵循以下规则：
                
                1. 直接回答用户的问题，不要返回JSON格式或代码块
                2. 回答要简洁、自然，像正常聊天一样
                3. 不要解释你的思考过程
                4. 不要输出任何格式标记（如```json等）
                5. 如果用户用中文提问，就用中文回答
                6. 保持回答在200字以内，除非用户要求详细说明
                
                现在开始与用户对话。
                """)
        Flux<String> chat(@UserMessage String userMessage,
                  @MemoryId String sessionId);
    }

