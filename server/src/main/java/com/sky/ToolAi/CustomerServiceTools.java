package com.sky.ToolAi;

import com.sky.entity.OrderDetail;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.OrderMapper;
import com.sky.service.OrderService;
import com.sky.vo.OrderVO;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component("customerServiceTools")
@Slf4j
public class CustomerServiceTools {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderService orderService;

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
        log.info("缓存回写成功，菜品数: {}", Optional.of(hotDish.size()));

        return hotDish;
    }

    @Tool("根据订单号查询订单id")
    public Long getIdByNumber(@P("订单号") String number){
        Long orderId = orderMapper.getOrderId(number);
        if(orderId==null){
            throw new OrderBusinessException("订单号不存在");
        }
        return orderId;
    }

    @Tool("根据订单id查询用户订单，提示用户订单的实时状态,userId从系统提示次里获取")
    public String getOrderById(@P("订单id") Long id, @P("当前用户ID") String userId) {
        OrderVO orderVO;
        try {
            orderVO = orderService.getByIdByAi(id,userId);
        } catch (Exception e) {
            return "未查询到该订单，请检查订单号是否正确";
        }

        // 订单状态映射
        String statusText = switch (orderVO.getStatus()) {
            case 1 -> "待付款";
            case 2 -> "待接单";
            case 3 -> "已接单";
            case 4 -> "派送中";
            case 5 -> "已完成";
            case 6 -> "已取消";
            default -> "未知状态";
        };

        // 拼接订单菜品
        StringBuilder dishes = new StringBuilder();
        if (orderVO.getOrderDetailList() != null) {
            for (OrderDetail detail : orderVO.getOrderDetailList()) {
                if (!dishes.isEmpty()) dishes.append("、");
                dishes.append(detail.getName()).append("x").append(detail.getNumber());
            }
        }

        return String.format("订单号：%s，状态：%s，菜品：%s，实付金额：%s元，下单时间：%s",
                orderVO.getNumber(), statusText, dishes, orderVO.getAmount(), orderVO.getOrderTime());
    }
    @Tool("根据订单id帮用户催单")
    public void reminderByAi(@P("订单id") Long id){
        orderService.reminder(id);
    }
}
