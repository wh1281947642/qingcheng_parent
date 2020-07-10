package com.qingcheng.service.order;
import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.order.Order;

import java.util.*;

/**
 * order业务逻辑层
 */
public interface OrderService {


    public List<Order> findAll();


    public PageResult<Order> findPage(int page, int size);


    public List<Order> findList(Map<String, Object> searchMap);


    public PageResult<Order> findPage(Map<String, Object> searchMap, int page, int size);


    public Order findById(String id);

    public Map<String,Object> add(Order order);


    public void update(Order order);


    public void delete(String id);

    /**
     * 批量发货
     *
     * @description
     * @author huiwang45@iflytek.com
     * @date 2020/04/01 16:16
     * @param orders
     * @return
     */
    public void batchSend(List<Order> orders);

    /**
      * 订单超时处理逻辑
      */
    public void orderTimeOutLogic();
}
