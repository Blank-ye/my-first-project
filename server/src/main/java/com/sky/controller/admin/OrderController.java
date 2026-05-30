package com.sky.controller.admin;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
/*
* 订单
* */
@RestController("adminOrderController")
@RequestMapping("/admin/order")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    /*
    * 订单搜索
    *
    * */
    @GetMapping("/conditionSearch")
    public Result<PageResult> search( OrdersPageQueryDTO ordersPageQueryDTO){
        log.info("订单搜索，{}",ordersPageQueryDTO);
        PageResult pageResult= orderService.search(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    /*
    * 各个状态的订单数量的状态
    *
    * */
    @GetMapping("/statistics")
    public Result<OrderStatisticsVO> orderStatus(){
        log.info("各个状态的订单数量的状态");
        OrderStatisticsVO orderStatisticsVO= orderService.selectStatus();
        return Result.success();
    }

    /*
    * 查询订单详细
    *
    * */
    @GetMapping("/details/{id}")
    public Result<OrderVO> selectOederDetail(@PathVariable Long id){
        log.info("查询订单详细，{}",id);
        OrderVO orderVO= orderService.getById(id);
        return Result.success(orderVO);
    }

    /*
    * 接单
    *
    * */
    @PutMapping("/confirm")
    public Result confirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO){
        log.info("接单，{}",ordersConfirmDTO);
        orderService.confirm(ordersConfirmDTO);
        return Result.success();
    }

    /*
    * 拒单
    *
    * */
    @PutMapping("/rejection")
    public Result rejection(@RequestBody OrdersRejectionDTO ordersRejectionDTO){
        log.info("拒单，{}",ordersRejectionDTO);
        orderService.rejection(ordersRejectionDTO);
        return Result.success();
    }

    /*
    * 取消订单
    *
    * */
    @PutMapping("/cancel")
    public Result cancel(@RequestBody OrdersCancelDTO ordersCancelDTO){
        log.info("取消订单，{}",ordersCancelDTO);
        orderService.cancel(ordersCancelDTO);
        return Result.success();
    }

    /*
    * 派送订单
    *
    * */
    @PutMapping("/delivery/{id}")
    public Result delivery(@PathVariable Long id){
        log.info("派送订单，{}",id);
        orderService.delivery(id);
        return Result.success();
    }

    /*
    *
    * 完成订单
    *
    * */
    @PutMapping("/complete/{id}")
    public Result complete(@PathVariable Long id){
        log.info("完成订单，{}",id);
        orderService.complete(id);
        return Result.success();
    }
}
