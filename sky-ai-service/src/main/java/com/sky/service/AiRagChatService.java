package com.sky.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;
import reactor.core.publisher.Flux;

/**
 * RAG (Retrieval-Augmented Generation) 对话服务
 *
 * 与普通对话的区别：
 * - 普通对话：用户问题 → LLM → 回答
 * - RAG 对话：用户问题 → 检索相关文档 → LLM + 上下文 → 回答
 *
 * RAG 的优势：
 * 1. 减少幻觉：LLM 基于真实文档回答，而非仅靠训练数据
 * 2. 知识更新：只需更新文档，无需重新训练模型
 * 3. 可追溯：可以知道回答基于哪些文档
 */
@AiService(
        wiringMode = AiServiceWiringMode.EXPLICIT,
        chatModel = "openAiChatModel",
        streamingChatModel = "openAiStreamingChatModel",
        contentRetriever = "embeddingStoreContentRetriever"
)
public interface AiRagChatService {

    @SystemMessage("""
            你是小仓外卖的智能客服。请根据以下规则回答用户问题：

            1. 根据提供的上下文信息回答问题，不要编造信息
            2. 如果上下文中没有相关信息，请如实告知用户
            3. 回答要简洁、自然，像正常聊天一样
            4. 如果用户用中文提问，就用中文回答
            5. 保持回答在200字以内，除非用户要求详细说明
            6. 不要返回JSON格式或代码块

            现在开始与用户对话。
            """)
    Flux<String> chat(@UserMessage String userMessage,
                      @MemoryId String sessionId);
}
