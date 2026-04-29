package com.sky.config;

import com.sky.ChatMemoryStore.RedisChatMemoryStore;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.service.MemoryId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Autowired
    private RedisChatMemoryStore redisChatMemoryStore;

    // 配置对话记忆：默认保存最近 10 条消息
    // 重写 ChatMemoryProvider 中的 get 方法
    @Bean("getMyChatMemoryProvider")
    //自定义的 ChatMemoryStore类的会话记忆提供者
    public ChatMemoryProvider getMyChatMemoryProvider() {
        return new ChatMemoryProvider() {
            @Override
            public ChatMemory get(Object memoryId) {
                return MessageWindowChatMemory.builder().
                        id(memoryId)
                        .maxMessages(10)
                        .chatMemoryStore(redisChatMemoryStore)
                        .build();
            }
        };
    }
}