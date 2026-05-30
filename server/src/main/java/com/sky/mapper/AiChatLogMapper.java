package com.sky.mapper;

import com.sky.dto.AiChatLogPageQueryDTO;
import com.sky.entity.AiChatLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AiChatLogMapper {

    @Insert("INSERT INTO ai_chat_log (user_id, user_message, ai_response, tool_name, input_tokens, output_tokens, total_tokens, create_time) " +
            "VALUES (#{userId}, #{userMessage}, #{aiResponse}, #{toolName}, #{inputTokens}, #{outputTokens}, #{totalTokens}, #{createTime})")
    void insert(AiChatLog aiChatLog);

    List<AiChatLog> pageQuery(AiChatLogPageQueryDTO dto);
}
