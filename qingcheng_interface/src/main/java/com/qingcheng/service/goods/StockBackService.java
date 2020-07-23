package com.qingcheng.service.goods;

import com.qingcheng.pojo.order.OrderItem;

import java.util.List;

public interface StockBackService {


    /**
     * 添加回滚记录
     * @description
     * @author huiwang45@iflytek.com
     * @date 2020/07/22 15:38
     * @param
     * @return
     */
    public void addList(List<OrderItem> orderItemList);


    /**
     * 执行库存回滚
     */
    public void doBack();
}
