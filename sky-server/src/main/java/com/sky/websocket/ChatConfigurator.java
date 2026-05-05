package com.sky.websocket;

import com.sky.constant.JwtClaimsConstant;
import com.sky.properties.JwtProperties;
import com.sky.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ChatConfigurator extends ServerEndpointConfig.Configurator {

    private static JwtProperties jwtProperties;

    @Autowired
    public void setJwtProperties(JwtProperties jwtProperties) {
        ChatConfigurator.jwtProperties = jwtProperties;
    }

    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
        try {
            if (jwtProperties == null) return;

            String uri = request.getRequestURI().toString();
            String token = uri.substring(uri.lastIndexOf("/") + 1);
            if (token.isEmpty()) return;

            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
            String userId = claims.get(JwtClaimsConstant.USER_ID).toString();
            sec.getUserProperties().put("userId", userId);
        } catch (Exception ignored) {
        }
    }
}
