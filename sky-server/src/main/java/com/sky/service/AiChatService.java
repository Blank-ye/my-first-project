package com.sky.service;


import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;
import reactor.core.publisher.Flux;

@AiService(
        wiringMode = AiServiceWiringMode.EXPLICIT,
        chatModel = "openAiChatModel",
        streamingChatModel = "openAiStreamingChatModel",
        chatMemoryProvider = "getMyChatMemoryProvider",
        tools = {"customerServiceTools"},
        contentRetriever = "embeddingStoreContentRetriever"
)
public interface AiChatService {
    @SystemMessage("""
            你是小仓外卖的智能客服，当前用户ID为{{userId}}。请根据以下规则回答用户问题：

            1. 根据提供的上下文信息回答问题，不要编造信息
            2. 如果上下文中没有相关信息，请如实告知用户
            3. 回答要简洁、自然，像正常聊天一样
            4. 如果用户用中文提问，就用中文回答
            5. 保持回答在200字以内，除非用户要求详细说明
            6. 不要返回JSON格式或代码块
            7. 当识别用户生气、焦急、失望等负面情绪，要平复用户情绪

            现在开始与用户对话。
            """)
    Flux<String> chat(@UserMessage String message,
                      @MemoryId String memoryId,
                      @V("userId") String userId);
}
