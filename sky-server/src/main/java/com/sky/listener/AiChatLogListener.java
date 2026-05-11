package com.sky.listener;

import com.sky.entity.AiChatLog;
import com.sky.mapper.AiChatLogMapper;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import dev.langchain4j.model.output.TokenUsage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class AiChatLogListener implements ChatModelListener {

    private static final ThreadLocal<Long> USER_ID_HOLDER = new ThreadLocal<>();

    @Autowired
    private AiChatLogMapper aiChatLogMapper;

    public static void setUserId(Long userId) {
        USER_ID_HOLDER.set(userId);
    }

    public static void clearUserId() {
        USER_ID_HOLDER.remove();
    }

    @Override
    public void onRequest(ChatModelRequestContext ctx) {
        log.debug("AI 请求发起");
    }

    @Override
    public void onResponse(ChatModelResponseContext ctx) {
        try {
            Long userId = USER_ID_HOLDER.get();
            if (userId == null) {
                log.warn("AiChatLogListener: userId 为空，跳过记录");
                return;
            }

            // 提取用户消息（取最后一条 UserMessage）
            String userMessage = "";
            List<ChatMessage> messages = ctx.chatRequest().messages();
            if (messages != null) {
                for (ChatMessage msg : messages) {
                    if (msg instanceof UserMessage) {
                        userMessage = ((UserMessage) msg).singleText();
                    }
                }
            }

            // 提取 AI 回答和工具调用
            String aiResponse = "";
            String toolName = "";
            AiMessage aiMessage = ctx.chatResponse().aiMessage();
            if (aiMessage != null) {
                if (aiMessage.text() != null) {
                    aiResponse = aiMessage.text();
                }
                if (aiMessage.hasToolExecutionRequests()) {
                    toolName = aiMessage.toolExecutionRequests().stream()
                            .map(req -> req.name())
                            .collect(Collectors.joining(","));
                }
            }

            // 提取 token 消耗
            int inputTokens = 0;
            int outputTokens = 0;
            int totalTokens = 0;
            TokenUsage tokenUsage = ctx.chatResponse().tokenUsage();
            if (tokenUsage != null) {
                inputTokens = tokenUsage.inputTokenCount() != null ? tokenUsage.inputTokenCount() : 0;
                outputTokens = tokenUsage.outputTokenCount() != null ? tokenUsage.outputTokenCount() : 0;
                totalTokens = tokenUsage.totalTokenCount() != null ? tokenUsage.totalTokenCount() : 0;
            }

            AiChatLog chatLog = AiChatLog.builder()
                    .userId(userId)
                    .userMessage(userMessage)
                    .aiResponse(aiResponse)
                    .toolName(toolName.isEmpty() ? null : toolName)
                    .inputTokens(inputTokens)
                    .outputTokens(outputTokens)
                    .totalTokens(totalTokens)
                    .createTime(LocalDateTime.now())
                    .build();

            aiChatLogMapper.insert(chatLog);
            log.info("AI 对话日志记录成功，userId={}, tool={}", userId, toolName);
        } catch (Exception e) {
            log.error("记录 AI 对话日志失败", e);
        }
    }

    @Override
    public void onError(ChatModelErrorContext ctx) {
        log.error("AI 调用出错: {}", ctx.error().getMessage());
    }
}
