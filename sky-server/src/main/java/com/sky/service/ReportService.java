package com.sky.service;

import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;

import java.time.LocalDate;

public interface ReportService {

    /*
    * 店铺营业额统计
    *
    * */
    TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end);

    /*
    * 统计用户数量
    *
    * */
    UserReportVO getuserStatistics(LocalDate begin, LocalDate end);

    /*
    * 订单统计
    *
    * */
    OrderReportVO getordersStatistics(LocalDate begin, LocalDate end);

    /*
    * 统计菜品top10
    *
    * */
    SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end);
}
