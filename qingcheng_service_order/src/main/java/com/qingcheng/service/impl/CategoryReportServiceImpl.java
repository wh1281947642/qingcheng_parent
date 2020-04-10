package com.qingcheng.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.qingcheng.dao.CategoryReportMapper;
import com.qingcheng.pojo.order.CategoryReport;
import com.qingcheng.service.order.CategoryReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service(interfaceClass = CategoryReportService.class)
public class CategoryReportServiceImpl implements CategoryReportService {

    @Autowired
    private CategoryReportMapper categoryReportMapper;

    /**
     *  类目统计
     * @description
     * @author huiwang45@iflytek.com
     * @date 2020/04/09 20:25
     * @param
     * @return
     */
    @Override
    public List<CategoryReport> categoryReport(LocalDate date) {
        return categoryReportMapper.categoryReport(date);
    }

    /**
     * 定时任务-生成统计数据
     * @description
     * @author huiwang45@iflytek.com
     * @date 2020/04/09 20:25
     * @param
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createData() {

        //查询昨天的类目统计数据
        //LocalDate localDate=LocalDate.now().minusDays(1);
        String strDate = "2019-04-15";
        String pat = "yyyy-MM-dd";
        //指定转换格式
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(pat);
        //进行转换
        LocalDate date = LocalDate.parse(strDate, fmt);
        List<CategoryReport> categoryReports = categoryReportMapper.categoryReport(date);

        //保存到tb_category_report
        categoryReports.forEach(categoryReport -> {
            categoryReportMapper.insert(categoryReport);
        });
    }

    @Override
    public List<Map> category1Count(String date1, String date2) {
        return categoryReportMapper.category1Count(date1,date2);
    }

}
