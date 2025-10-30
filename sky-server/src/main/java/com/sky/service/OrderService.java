package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

public interface OrderService {

    /*
    * 用户下单
    *
    * */
    OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO);
    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功，修改订单状态
     * @param outTradeNo
     */
    void paySuccess(String outTradeNo);

    /*
    * 查看历史订单
    *
    * */
    PageResult history(OrdersPageQueryDTO ordersPageQueryDTO);

    /*
    * 查看订单详细
    *
    * */
    OrderVO getById(Long id);

    /*
    * 再来一单
    *
    * */
    void repetition(Long id);

    /*
    * 取消订单
    *
    * */
    void cancelByid(Long id);

    /*
    * 搜索订单
    *
    * */
    PageResult search(OrdersPageQueryDTO ordersPageQueryDTO);

    /*
    * 查看各个订单状态的数量
    *
    * */
    OrderStatisticsVO selectStatus();

    /*
    * 接单
    *
    * */
    void confirm(OrdersConfirmDTO ordersConfirmDTO);

    /*
    * 拒单
    *
    * */
    void rejection(OrdersRejectionDTO ordersRejectionDTO);

    /*
    * 取消订单
    *
    * */
    void cancel(OrdersCancelDTO ordersCancelDTO);

    /*
    * 派送订单
    *
    * */
    void delivery(Long id);

    /*
    * 完成订单
    *
    * */
    void complete(Long id);

    /*
    * 催单
    *
    * */
    void reminder(Long id);
}
