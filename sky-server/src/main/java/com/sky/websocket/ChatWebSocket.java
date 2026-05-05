package com.sky.websocket;

import com.sky.service.AiChatService;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint(value = "/ws/chat/{token}", configurator = ChatConfigurator.class)
public class ChatWebSocket {

    private static AiChatService aiChatService;

    @Autowired
    public void setAiChatService(AiChatService aiChatService) {
        ChatWebSocket.aiChatService = aiChatService;
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

        Disposable old = SUBSCRIPTIONS.get(userId);
        if (old != null && !old.isDisposed()) {
            old.dispose();
        }

        Disposable disposable = aiChatService.chat(message, userId, userId)
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
