package com.sky.ToolAi;

import com.sky.mapper.OrderMapper;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component("customerServiceTools")
@Slf4j
public class CustomerServiceTools {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final DefaultRedisScript<Long> LPUSH;
    static {
        LPUSH = new DefaultRedisScript<>();
        LPUSH.setLocation(new ClassPathResource("Lpush.lua"));
        LPUSH.setResultType(Long.class);
    }

    private static final String KEY = "daily:dishes";

    @Tool("向用户推荐热门菜品")
    public List<String> getDailyDishs() {
        // 1. 先从 Redis 缓存取
        List<String> cached = stringRedisTemplate.opsForList().range(KEY, 0, -1);
        if (cached != null && !cached.isEmpty()) {
            return cached;
        }

        // 2. 缓存未命中，查数据库并回写缓存
        List<String> hotDish = orderMapper.getHotDish();
        if (hotDish == null || hotDish.isEmpty()) {
            log.info("兜底：数据库无热门菜品");
            return Collections.emptyList();
        }

        // 3. 写入 Redis 缓存
        List<String> args = new ArrayList<>(hotDish);
        args.add(String.valueOf(Duration.ofHours(25).getSeconds()));
        stringRedisTemplate.execute(LPUSH, Collections.singletonList(KEY), args.toArray());
        log.info("缓存回写成功，菜品数: {}", hotDish.size());

        return hotDish;
    }

    
}
