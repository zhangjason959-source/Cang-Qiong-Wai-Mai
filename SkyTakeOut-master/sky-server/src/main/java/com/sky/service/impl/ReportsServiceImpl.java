package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportsService;
import com.sky.service.WorkSpaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.poi.ss.formula.functions.T;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@Slf4j
public class ReportsServiceImpl implements ReportsService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WorkSpaceService workSpaceService;


    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {

//        ArrayList<LocalDate> dateList = new ArrayList<>();
//        dateList.add(begin);
//
//        while (!begin.equals(end)){
//            begin=begin.plusDays(1);//日期计算，获得指定日期后1天的日期
//            dateList.add(begin);
//        }
//        ArrayList<Double> turnoverList = new ArrayList<>();
//        for (LocalDate localDate : dateList) {
//            //设置每天开始的时间
//            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
//            //设置每天结束的时间
//            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);
//            //封装要查询的对象
//            Map map = new HashMap<>();
//            map.put("status", Orders.COMPLETED);
//            map.put("begin", beginTime);
//            map.put("end", endTime);
//            Double turnover = orderMapper.sumByMap(map);
//            turnover = turnover == null ? 0.0 : turnover;
//            turnoverList.add(turnover);
//        }
////返回查询到的对象并进行封装
//        return TurnoverReportVO.builder()
//                .dateList(StringUtils.join(dateList, ","))
//                .turnoverList(StringUtils.join(turnoverList, ","))
//                .build();

        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while(!begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }


        String dates = "";
        for(LocalDate date : dateList){
            dates += date.toString()+",";
        }

        String orderNumbers = "";
        for(LocalDate date : dateList){
            LocalDateTime beginTime = LocalDateTime.of(date, LocalDateTime.MIN.toLocalTime());
            LocalDateTime endTime = LocalDateTime.of(date, LocalDateTime.MAX.toLocalTime());
            Double sum = orderMapper.getOrdersByBeginAndEnd(5,beginTime,endTime);
            if(sum==null)
                sum = 0.0;
            orderNumbers += sum+",";
        }

        return TurnoverReportVO.builder().dateList( dates).turnoverList(orderNumbers).build();
    }

    @Override
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {
//        ArrayList<LocalDate> dateList = dataInsert(begin, end);
//        ArrayList<Integer> newCustomers = new ArrayList<>();
//        for (LocalDate localDate : dateList) {
//            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
//            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);
//            Map map=new HashMap<>();
//            map.put("beginTime",beginTime);
//            map.put("endTime",endTime);
//            Integer newCustom=userMapper.queryNewCustomers(map);
//            newCustom = newCustom==null?0:newCustom;
//            newCustomers.add(newCustom);
//        }
//
//        return UserReportVO.builder()
//                .dateList(StringUtils.join(dateList,","))
//                .newUserList(StringUtils.join(newCustomers,","))
//                .totalUserList(StringUtils.join(userMapper.queryAll(),","))
//                .build();
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while(!begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        String dates = "";
        for(LocalDate date : dateList){
            dates += date.toString()+",";
        }

        String newUserList = "";
        String totalUserList = "";
        for(LocalDate date : dateList){
            LocalDateTime beginTime = LocalDateTime.of(date, LocalDateTime.MIN.toLocalTime());
            LocalDateTime endTime = LocalDateTime.of(date, LocalDateTime.MAX.toLocalTime());
            Integer newUser = userMapper.getNumByBeginAndEnd(beginTime,endTime);
            Integer totalUser = userMapper.getNumByBeginAndEnd(null,LocalDateTime.of(end, LocalDateTime.MIN.toLocalTime()));
            if(newUser==null)
                newUser = 0;
            newUserList += newUser+",";
            if(totalUser==null)
                totalUser = 0;
            totalUserList += totalUser+",";
        }


        return UserReportVO.builder().dateList( dates).newUserList(newUserList).totalUserList(totalUserList).build();
    }

    @Override
    public OrderReportVO orderStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while(!begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        String orderCountList = "";
        String validOrderCountList = "";
        Integer total = 0;
        double rate = 0.0;
        Integer valid = 0;

        for(LocalDate date : dateList){
            LocalDateTime beginTime = LocalDateTime.of(date, LocalDateTime.MIN.toLocalTime());
            LocalDateTime endTime = LocalDateTime.of(date, LocalDateTime.MAX.toLocalTime());
            Integer totalOrder = orderMapper.todayOrderByCount(null,beginTime,endTime);
            orderCountList += totalOrder+",";
            total += totalOrder;
            Integer validOrder = orderMapper.todayOrderByCount(5,beginTime,endTime);
            validOrderCountList += validOrder+",";
            valid += validOrder;
        }
        if(total!=0){
            rate = valid.doubleValue()/total.doubleValue();
        }


        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .orderCompletionRate(rate)
                .orderCountList(orderCountList)
                .totalOrderCount( total)
                .validOrderCount( valid)
                .validOrderCountList(validOrderCountList)
                .build();
    }

    @Override
    public SalesTop10ReportVO top10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalDateTime.MIN.toLocalTime());
        LocalDateTime endTime = LocalDateTime.of(end, LocalDateTime.MAX.toLocalTime());

        String nameList = "";
        String numberList = "";
        List<GoodsSalesDTO> list = orderMapper.getSalesTop10(beginTime,endTime);
        for (GoodsSalesDTO goodsSalesDTO : list) {
            nameList += goodsSalesDTO.getName() + ",";
            numberList += goodsSalesDTO.getNumber() + ",";
        }
        return SalesTop10ReportVO.builder().nameList(nameList).numberList(numberList).build();
    }

    @Override
    public void exportBusinessData(HttpServletResponse response) {
        LocalDate beginTime = LocalDate.now().minusDays(30);
        LocalDate endTime = LocalDate.now().minusDays(1);

        BusinessDataVO businessDataVO =workSpaceService.getBusinessData(LocalDateTime.of(beginTime, LocalTime.MIN), LocalDateTime.of(endTime, LocalTime.MAX));
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");

        try {
            XSSFWorkbook excel = new XSSFWorkbook(in);
            XSSFSheet sheet = excel.getSheet("Sheet1");
            sheet.getRow(1).getCell(1).setCellValue(beginTime + "至" + endTime);
            sheet.getRow(3).getCell(2).setCellValue(businessDataVO.getTurnover());
            sheet.getRow(3).getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            sheet.getRow(3).getCell(6).setCellValue(businessDataVO.getNewUsers());
            sheet.getRow(4).getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            sheet.getRow(4).getCell(4).setCellValue(businessDataVO.getUnitPrice());

            for(int i = 0;i < 30 ; i++){
                LocalDate date = beginTime.plusDays(i);
                BusinessDataVO data = workSpaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));
                sheet.getRow(7+i).getCell(1).setCellValue(date.toString());
                sheet.getRow(7+i).getCell(2).setCellValue(data.getTurnover());
                sheet.getRow(7+i).getCell(3).setCellValue(data.getValidOrderCount());
                sheet.getRow(7+i).getCell(4).setCellValue(data.getOrderCompletionRate());
                sheet.getRow(7+i).getCell(5).setCellValue(data.getUnitPrice());
                sheet.getRow(7+i).getCell(6).setCellValue(data.getNewUsers());
            }
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);
            out.close();
            excel.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
