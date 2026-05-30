package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
/*
* 店铺的数据统计
* */
@RestController
@RequestMapping("/admin/report")
@Slf4j
public class ReportController {

    @Autowired
    private ReportService reportService;

    /*
    * 店铺营业额统计
    *
    * */
    @GetMapping("/turnoverStatistics")
    public Result<TurnoverReportVO> turnoverStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end){
        log.info("店铺营业额统计，{}，{}",begin,end);
        TurnoverReportVO turnoverReportVO= reportService.getTurnoverStatistics(begin,end);
        return Result.success(turnoverReportVO);

    }

    /*
    * 用户数量统计
    *
    * */
    @GetMapping("/userStatistics")
    public Result<UserReportVO> userStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end){
        log.info("用户数量统计，{}，{}",begin,end);
        return Result.success(reportService.getuserStatistics(begin,end));
    }

    /*
     * 订单统计
     *
     * */
    @GetMapping("/ordersStatistics")
    public Result<OrderReportVO> ordersStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end){
        log.info("订单统计，{}，{}",begin,end);
        return Result.success(reportService.getordersStatistics(begin,end));
    }

    /*
     * 统计菜品top10
     *
     * */
    @GetMapping("/top10")
    public Result<SalesTop10ReportVO> top10(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end){
        log.info("统计菜品top10，{}，{}",begin,end);
        return Result.success(reportService.getSalesTop10(begin,end));
    }

    /*
    * 导出excel表个
    *
    * */
    @GetMapping("/export")
    public void export(HttpServletResponse response){
        reportService.getExcel(response);
    }
}
