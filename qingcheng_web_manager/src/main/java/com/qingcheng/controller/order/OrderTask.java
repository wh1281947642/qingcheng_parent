package com.qingcheng.controller.order;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.service.order.CategoryReportService;
import com.qingcheng.service.order.OrderService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * <p>
 * <code>OrderTask</code>
 * </p>
 *
 * @author huiwang45@iflytek.com
 * @description
 * @date 2020/04/02 17:22
 */

@Component
public class OrderTask {

    @Reference
    private OrderService orderService;

    @Reference
    private CategoryReportService categoryReportService;


    /*@Scheduled(cron = "* * * * * ?")
    public void orderTimeOutLogic(){
        System.out.println(new Date());
    }*/

   /**
    * 订单超时未付款 自动关闭
    * @description
    * @author huiwang45@iflytek.com
    * @date 2020/04/03 15:02
    * @param
    * @return
    */
    /*@Scheduled(cron = "0 0/2 * * * ?")
    public void orderTimeOutLogic(){
        System.out.println("每两分钟间隔执行一次任务"+ new Date());
        orderService.orderTimeOutLogic();
    }*/

  /* @Scheduled(cron = "0 0 1 * * ?")
    public void createCategoryReportDate(){
        System.out.println("生成类目统计数据");
        categoryReportService.createData();
    }*/

}
