package com.sky.Task;

import com.sky.entity.Dish;
import com.sky.entity.HotDish;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.Schedules;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.View;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/*
* 定时任务类
*
* */
@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final DefaultRedisScript<Long> LPUSH;
    static {
            LPUSH=new DefaultRedisScript<>();
            LPUSH.setLocation(new ClassPathResource("Lpush.lua"));
            LPUSH.setResultType(Long.class);
    }


    /*
    * 每分钟处理一直在待支付状态的订单
    *
    * */
    @Scheduled(cron = "0 * * * * ?")
    public void processTimeOrder(){
        log.info("处理在待支付状态的订单，{}", LocalDateTime.now());

        //获取十五分钟前的时间
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);

        //根据状态和时间查询订单
        List<Orders> orders=orderMapper.getByStatusAndTime(Orders.PAID ,time);

        //判断订单是否为空
        if (orders !=null && orders.size()>0){
            for (Orders order : orders) {
                order.setStatus(Orders.CANCELLED);
                order.setCancelReason("下单超时");
                order.setCancelTime(LocalDateTime.now());
                orderMapper.update(order);
            }
        }
    }

    /*
    * 没天的凌晨一点处理在配送中的订单
    *
    * */
    @Scheduled(cron = "0 0 1 * * ?")
    public void processDeliveryOrder(){
        log.info("处理在配送中状态的订单，{}", LocalDateTime.now());

        //获取一小时前的时间
        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);

        //根据状态和时间查询订单
        List<Orders> orders=orderMapper.getByStatusAndTime(Orders.DELIVERY_IN_PROGRESS ,time);

        //判断订单是否为空
        if (orders !=null && orders.size()>0){
            for (Orders order : orders) {
                order.setStatus(Orders.COMPLETED);
                orderMapper.update(order);
            }
        }
    }


    @Scheduled(cron = "0 0 3 ? * SUN")   // 每周日凌晨3点
    @Transactional
    public void refreshWeeklyTop5() {
        orderMapper.truncate();
        orderMapper.insertWeeklyTop5();
    }

    /*@Scheduled(cron = "0 0 4 ? * *")
    public void refreshDailyDishes() {
        String key = "daily:dishes";
        Mono.fromCallable(() ->  orderMapper.getHotDish())
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(dishNames ->
                        redisTemplate.delete(key)
                                .then(redisTemplate.opsForList()
                                        .leftPushAll(key, dishNames))
                        .then(redisTemplate.expire(key, Duration.ofHours(25)))
                )
                .subscribe(
                        null,
                        error -> log.error("刷新菜品数据失败：{}", error.getMessage()),
                        () -> log.info("刷新菜品数据成功")
                );

    }*/

    @Scheduled(cron = "0 0 4 ? * *")
    public void refreshDailyDishes() {
        String key = "daily:dishes";
        try {
            List<String> hotDish = orderMapper.getHotDish();
            if(hotDish != null && hotDish.isEmpty()){
                log.info("菜品为空");
                return;
            }
            List<String> list = new ArrayList<>(hotDish);
            list.add(String.valueOf(Duration.ofHours(25).getSeconds()));
            Long execute = stringRedisTemplate.execute(
                    LPUSH,
                    Collections.singletonList(key),
                    list.toArray()
            );
            log.info("每日菜品缓存刷新成功，结果码: {}",execute);
        } catch (Exception e) {
            log.info("菜品缓存失败",e);
        }
    }
}
