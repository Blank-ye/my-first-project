package com.sky.websocket;

import com.sky.ToolAi.InputSanitizer;
import com.sky.ToolAi.RateLimiter;
import com.sky.service.AiChatService;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@ServerEndpoint(value = "/ws/chat/{token}", configurator = ChatConfigurator.class)
public class ChatWebSocket {

    private static AiChatService aiChatService;
    private static RateLimiter rateLimiter;

    @Autowired
    public void setAiChatService(AiChatService aiChatService) {
        ChatWebSocket.aiChatService = aiChatService;
    }

    @Autowired
    public void setRateLimiter(RateLimiter rateLimiter) {
        ChatWebSocket.rateLimiter = rateLimiter;
    }

    private static final Map<String, Session> SESSIONS = new ConcurrentHashMap<>();
    private static final Map<String, Disposable> SUBSCRIPTIONS = new ConcurrentHashMap<>();





    @OnOpen
    public void onOpen(Session session) {
        String userId = (String) session.getUserProperties().get("userId");
        if (userId == null) {
            try {
                session.getBasicRemote().sendText("[ERROR]认证失败");
                session.close();
            } catch (Exception ignored) {}
            return;
        }
        SESSIONS.put(userId, session);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        String userId = (String) session.getUserProperties().get("userId");
        if (userId == null) return;

        // 1. 速率限制
        if (!rateLimiter.isChatAllowed(userId)) {
            try {
                session.getBasicRemote().sendText("[ERROR]操作太频繁，请稍后再试");
            } catch (Exception ignored) {}
            return;
        }

        // 2. 输入清洗
        String sanitized = InputSanitizer.sanitize(message);
        if (sanitized == null) {
            log.warn("检测到疑似 Prompt 注入，用户: {}, 输入: {}", userId, message);
            try {
                session.getBasicRemote().sendText("[ERROR]输入内容不合规，请重新提问");
            } catch (Exception ignored) {}
            return;
        }

        // 3. 取消上一次未完成的请求
        Disposable old = SUBSCRIPTIONS.get(userId);
        if (old != null && !old.isDisposed()) {
            old.dispose();
        }

        // 4. 调用 AI
        Disposable disposable = aiChatService.chat(sanitized, userId, userId)
                .subscribe(
                        token -> {
                            try {
                                session.getBasicRemote().sendText(token);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        },
                        error -> {
                            try {
                                session.getBasicRemote().sendText("[ERROR]" + error.getMessage());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        },
                        () -> {
                            try {
                                session.getBasicRemote().sendText("[DONE]");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                );

        SUBSCRIPTIONS.put(userId, disposable);
    }

    @OnClose
    public void onClose(Session session) {
        String userId = (String) session.getUserProperties().get("userId");
        if (userId != null) {
            SESSIONS.remove(userId);
            Disposable disposable = SUBSCRIPTIONS.remove(userId);
            if (disposable != null && !disposable.isDisposed()) {
                disposable.dispose();
            }
        }
    }
}
