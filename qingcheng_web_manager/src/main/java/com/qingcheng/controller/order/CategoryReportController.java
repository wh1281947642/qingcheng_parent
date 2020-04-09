package com.qingcheng.controller.order;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.pojo.order.CategoryReport;
import com.qingcheng.service.order.CategoryReportService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/categoryReport")
public class CategoryReportController {

    @Reference
    private CategoryReportService categoryReportService;

    /**
     * 昨天的数据统计（商品类目）
     * @return
     */
    @GetMapping("/yesterday")
    public List<CategoryReport> yesterday(){
        LocalDate localDate= LocalDate.now().minusDays(1);//得到昨天的日期
        //return categoryReportService.categoryReport(localDate);

        String strDate = "2019-04-15";
        String pat = "yyyy-MM-dd";
        //指定转换格式
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(pat);
        //进行转换
        LocalDate date = LocalDate.parse(strDate, fmt);
        return categoryReportService.categoryReport(date);
    }



    @GetMapping("/category1Count")
    public List<Map> category1Count(String date1,String date2){
        return categoryReportService.category1Count(date1,date2);
    }


    public static void main(String[] args) {

        LocalDate localDate= LocalDate.now().minusDays(1);//得到昨天的日期
        LocalDate now = LocalDate.now();
        System.out.println(localDate);
        System.out.println(now);

        String strDate = "2019-04-15";
        String pat = "yyyy-MM-dd";
        //指定转换格式
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(pat);
        //进行转换
        LocalDate date = LocalDate.parse(strDate, fmt);

        System.out.println(date);

    }


}
