package com.sky.ChatMemoryStore;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;

@Repository
public class RedisChatMemoryStore implements ChatMemoryStore {

    @Autowired
    private RedisTemplate redisTemplate;

    private static final String KEY="chat:memory:";

    @Override
    public List<ChatMessage> getMessages(Object MemoryId) {
        // 从redis中获取数据
        String json =(String) redisTemplate.opsForValue().get(KEY+MemoryId);
        //将数据转换成List<ChatMessage>
        List<ChatMessage> list = ChatMessageDeserializer.messagesFromJson(json);
        return list;
    }

    @Override
    public void updateMessages(Object MemoryId, List<ChatMessage> list) {

        //创建key
        String key = KEY + MemoryId;

        // 将list转换成json
        String json = ChatMessageSerializer.messagesToJson(list);
        //将json保存到redis中
        redisTemplate.opsForValue().set(key,json, Duration.ofDays(1));
    }

    @Override
    public void deleteMessages(Object MemoryId) {
        //删除redis中的数据
        redisTemplate.delete(KEY+MemoryId);
    }
}
