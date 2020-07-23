package com.qingcheng.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.qingcheng.dao.SkuMapper;
import com.qingcheng.dao.StockBackMapper;
import com.qingcheng.pojo.goods.StockBack;
import com.qingcheng.pojo.order.OrderItem;
import com.qingcheng.service.goods.StockBackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * <code>StockBackServiceImpl</code>
 * </p>
 * 
 * @author huiwang45@iflytek.com
 * @description
 * @date 2020/07/22 16:12
 */
@Service(interfaceClass = StockBackService.class)
public class StockBackServiceImpl implements StockBackService {

    @Autowired
    private StockBackMapper stockBackMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addList(List<OrderItem> orderItemList) {
        orderItemList.forEach(orderItem -> {
            StockBack stockBack = new StockBack();
            stockBack.setOrderId(orderItem.getOrderId());
            stockBack.setSkuId(orderItem.getSkuId());
            stockBack.setStatus("0");
            stockBack.setNum(orderItem.getNum());
            stockBack.setCreateTime(new Date());
            stockBackMapper.insertSelective(stockBack);
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void doBack() {
        System.out.println("库存回滚任务开始！");
        //查询库存回滚表中状态为0的记录
        StockBack stockBack = new StockBack();
        stockBack.setStatus("0");
        //需要回滚的列表
        List<StockBack> backList = this.stockBackMapper.select(stockBack);

        backList.forEach(stockBack1 -> {
            //扣减库存方法 添加库存
            skuMapper.deductionStock(stockBack1.getSkuId(), -stockBack1.getNum());
            //添加销量 减少销量
            skuMapper.addSaleNum(stockBack1.getSkuId(), -stockBack1.getNum());
            stockBack1.setStatus("1");
            stockBackMapper.updateByPrimaryKeySelective(stockBack1);
        });
        System.out.println("库存回滚任务结束");
    }
}
