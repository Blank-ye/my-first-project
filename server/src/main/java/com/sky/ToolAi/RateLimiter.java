package com.sky.ToolAi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * 基于 Redis 的滑动窗口限流器
 */
@Component
public class RateLimiter {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final DefaultRedisScript<Long> RATE_LIMIT_SCRIPT;

    static {
        RATE_LIMIT_SCRIPT = new DefaultRedisScript<>();
        RATE_LIMIT_SCRIPT.setLocation(new ClassPathResource("rate-limit.lua"));
        RATE_LIMIT_SCRIPT.setResultType(Long.class);
    }

    /**
     * 检查是否允许请求
     * @param key 限流 key
     * @param limit 窗口内最大请求数
     * @param windowSeconds 窗口大小（秒）
     */
    public boolean isAllowed(String key, int limit, int windowSeconds) {
        Long result = redisTemplate.execute(
            RATE_LIMIT_SCRIPT,
            Collections.singletonList("rate:" + key),
            String.valueOf(limit),
            String.valueOf(windowSeconds)
        );
        return result != null && result == 1;
    }

    /**
     * AI 聊天限流：每用户每分钟最多 10 次
     */
    public boolean isChatAllowed(String userId) {
        return isAllowed("chat:" + userId, 10, 60);
    }
}
