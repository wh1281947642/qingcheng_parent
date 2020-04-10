package com.qingcheng.service.order;

import com.qingcheng.pojo.order.CategoryReport;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface CategoryReportService {

    /**
     *  类目统计
     * @description
     * @author huiwang45@iflytek.com
     * @date 2020/04/09 20:25
     * @param
     * @return
     */
    public List<CategoryReport> categoryReport(LocalDate date);

    /**
     * 定时任务-生成统计数据
     * @description
     * @author huiwang45@iflytek.com
     * @date 2020/04/09 20:25
     * @param
     * @return
     */
    public void createData();

    /**
     * 按日期统计一级分类数据
     * @description
     * @author huiwang45@iflytek.com
     * @date 2020/04/09 21:20
     * @param
     * @return
     */
    public List<Map> category1Count(String date1, String date2);

}
