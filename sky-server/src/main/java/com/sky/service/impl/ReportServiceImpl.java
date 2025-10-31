package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;

    /*
    * 店铺营业额统计
    *
    * */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        //计算时间的区间
        List<LocalDate> localDates =new ArrayList<>();

        localDates.add(begin);
        while (!begin.equals(end)){
            begin=begin.plusDays(1);
            localDates.add(begin);
        }

        List<Double> turnoverList=new ArrayList<>();
        //遍历集合
        for (LocalDate localDate : localDates) {
            //记录开始时间和结束时间
            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);

            //将开始时间，结束时间和状态存入map集合中
            Map map=new HashMap<>();
            map.put("begin",beginTime);
            map.put("end",endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover= orderMapper.getTurnover(map);
            turnover= turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        }

        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(localDates,","))
                .turnoverList(StringUtils.join(turnoverList,","))
                .build();
    }

    /*
    * 统计用户数量
    *
    * */
    @Override
    public UserReportVO getuserStatistics(LocalDate begin, LocalDate end) {
        //计算时间的区间
        List<LocalDate> localDates =new ArrayList<>();

        localDates.add(begin);
        while (!begin.equals(end)){
            begin=begin.plusDays(1);
            localDates.add(begin);
        }
        //创建新用户和用户总数的集合
        List<Integer> newUsers=new ArrayList<>();
        List<Integer> totalUsers=new ArrayList<>();
        //遍历时间集合
        for (LocalDate localDate : localDates) {
            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);

            Map map=new HashMap<>();
            map.put("end",endTime);
            //查询用户总人数
            Integer newUser= userMapper.getByMap(map);
            map.put("begin",beginTime);
            Integer totalUser= userMapper.getByMap(map);
            newUsers.add(newUser);
            totalUsers.add(totalUser);
        }
        return UserReportVO.builder()
                .dateList(StringUtils.join(localDates,","))
                .newUserList(StringUtils.join(newUsers,","))
                .totalUserList(StringUtils.join(totalUsers,","))
                .build();
    }

    @Override
    public OrderReportVO getordersStatistics(LocalDate begin, LocalDate end) {
        //计算时间的区间
        List<LocalDate> localDates =new ArrayList<>();

        localDates.add(begin);
        while (!begin.equals(end)){
            begin=begin.plusDays(1);
            localDates.add(begin);
        }
        //创建新用户和用户总数的集合
        List<Integer> orderCountList=new ArrayList<>();
        List<Integer> validOrderCountList=new ArrayList<>();
        //遍历时间集合
        for (LocalDate localDate : localDates) {
            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);

            Map map=new HashMap();
            map.put("begin",beginTime);
            map.put("end",endTime);
            //统计订单总数
            Integer orderCount= orderMapper.countByMap(map);
            map.put("status",Orders.COMPLETED);
            Integer validOrderCount= orderMapper.countByMap(map);

            orderCountList.add(orderCount);
            validOrderCountList.add(validOrderCount);
        }
        //计算订单数的总和
        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();
        //计算有效订单总数
        Integer validOrderCounts = validOrderCountList.stream().reduce(Integer::sum).get();

        Double orderCompletionRate=0.0;
        if (totalOrderCount !=null){
            orderCompletionRate=validOrderCounts.doubleValue()/totalOrderCount;
        }
        return OrderReportVO.builder()
                .dateList(StringUtils.join(localDates,","))
                .orderCountList(StringUtils.join(orderCountList,","))
                .validOrderCountList(StringUtils.join(validOrderCountList,","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCounts)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    /*
    * 统计菜品top10
    *
    * */
    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        List<GoodsSalesDTO> salesTop10 = orderMapper.getSalesTop10(beginTime, endTime);

        List<String> names = salesTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());

        List<Integer> numbers = salesTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());

        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(names,","))
                .numberList(StringUtils.join(numbers,","))
                .build();
    }
}
