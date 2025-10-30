package com.sky.Task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.Schedules;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/*
* 定时任务类
*
* */
@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

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
}
