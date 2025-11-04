package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.*;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.extractor.XSSFExcelExtractor;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
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
    @Autowired
    private WorkspaceServiceImpl workspaceService;

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
            Integer totalUser= userMapper.getByMap(map);
            map.put("begin",beginTime);
            Integer newUser= userMapper.getByMap(map);
            newUsers.add(newUser);
            totalUsers.add(totalUser);
        }
        return UserReportVO.builder()
                .dateList(StringUtils.join(localDates,","))
                .newUserList(StringUtils.join(newUsers,","))
                .totalUserList(StringUtils.join(totalUsers,","))
                .build();
    }

    /*
     * 订单统计
     *
     * */
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

    /*
     * 导出Excel表格
     *
     * */
    @Override
    public void getExcel(HttpServletResponse response) {
        //计算时间
        LocalDate Date = LocalDate.now();

        //过去30天
        LocalDate beginDate = Date.minusDays(30);
        LocalDate endDate = Date.minusDays(1);

        //查新过去30天的营业状况
        BusinessDataVO businessDataVO = workspaceService.getBusinessData(LocalDateTime.of(beginDate, LocalTime.MIN), LocalDateTime.of(endDate, LocalTime.MAX));


        //基于当前文件创建一个excel文件
        try(InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
            ServletOutputStream out = response.getOutputStream();
            XSSFWorkbook excel = new XSSFWorkbook(in);) {
            //获取sheet页
            XSSFSheet sheet = excel.getSheet("Sheet1");
            //在第二行第二个单元格中填充数据
            sheet.getRow(1).getCell(1).setCellValue("时间："+beginDate+"至"+endDate);
            //获取第四行
            XSSFRow row = sheet.getRow(3);
            //填充数据
            row.getCell(2).setCellValue(businessDataVO.getTurnover());
            row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessDataVO.getNewUsers());
            //获取第五行
            row= sheet.getRow(4);
            row.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            row.getCell(4).setCellValue(businessDataVO.getUnitPrice());

            //填充明细数据
            for (int i = 0; i < 30; i++) {
                //计算日期
                LocalDate date = beginDate.plusDays(i);
                //查询某一天的数据
                BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));
                //获取某一行
                row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(businessData.getTurnover());
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                row.getCell(6).setCellValue(businessData.getNewUsers());

            }

            excel.write(out);

        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
