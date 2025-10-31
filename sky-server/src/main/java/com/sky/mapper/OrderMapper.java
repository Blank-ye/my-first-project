package com.sky.mapper;

import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.SalesTop10ReportVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    /*
    * 向下单表中插入一条数据
    *
    * */
    void insert(Orders orders);

    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    /*
    * 查看历史订单
    *
    * */
    List<Orders> page(OrdersPageQueryDTO ordersPageQueryDTO);

    /*
    * 根据id查询订单
    *
    * */
    @Select("select * from orders where id=#{id}")
    Orders getById(Long id);

    /*
    * 查询待接单的数量
    *
    * */
    @Select("select count(*) from orders where status=#{status}")
    Integer status(Integer status);

    /*
    * 处理定时任务
    *
    * */
    @Select("select * from orders where status=#{paid} and order_time<#{time}")
    List<Orders> getByStatusAndTime(Integer paid, LocalDateTime time);

    /*
    * 统计营业额
    *
    * */
    Double getTurnover(Map map);

    /*
    * 统计订单数
    *
    * */
    Integer countByMap(Map map);

    /*
    * 统计菜品top10
    *
    * */
    List<GoodsSalesDTO> getSalesTop10(LocalDateTime begin, LocalDateTime end);
}
