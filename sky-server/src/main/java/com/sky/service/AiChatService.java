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
    @SystemMessage(fromResource = "AiRag/system-prompt.txt")
    Flux<String> chat(@UserMessage String message,
                      @MemoryId String memoryId,
                      @V("userId") String userId);
}
