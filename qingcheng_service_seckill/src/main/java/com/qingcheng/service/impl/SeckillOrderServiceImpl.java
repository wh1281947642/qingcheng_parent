/*
package com.qingcheng.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.qingcheng.dao.SeckillGoodsMapper;
import com.qingcheng.pojo.seckill.SeckillGoods;
import com.qingcheng.pojo.seckill.SeckillOrder;
import com.qingcheng.service.seckill.SeckillOrderService;
import com.qingcheng.util.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Date;

*/
/****
 * @Author:itheima
 * @Date:2019/5/27 18:06
 * @Description:
 *****//*

@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private IdWorker idWorker;

    */
/***
     * 下单实现
     * @param id:商品ID
     * @param time:商品时区
     * @param username:用户名
     * @return
     *//*

    @Override
    public Boolean add(Long id, String time, String username) {
        //查询商品详情
        SeckillGoods goods = (SeckillGoods) redisTemplate.boundHashOps("SeckillGoods_"+time).get(id);

        if(goods!=null && goods.getStockCount()>0){
            //创建订单
            SeckillOrder seckillOrder = new SeckillOrder();
            seckillOrder.setId(idWorker.nextId());
            seckillOrder.setSeckillId(id);
            seckillOrder.setMoney(goods.getCostPrice());
            seckillOrder.setUserId(username);
            seckillOrder.setSellerId(goods.getSellerId());
            seckillOrder.setCreateTime(new Date());
            seckillOrder.setStatus("0");
            redisTemplate.boundHashOps("SeckillOrder").put(username,seckillOrder);

            //库存削减
            goods.setStockCount(goods.getStockCount()-1);
            //商品库存=0->将数据同步到MySQL，并清理Redis缓存
            if(goods.getStockCount()<=0){
                seckillGoodsMapper.updateByPrimaryKeySelective(goods);
                //清理Redis缓存
                redisTemplate.boundHashOps("SeckillGoods_"+time).delete(id);
            }else{
                //将数据同步到Redis
                redisTemplate.boundHashOps("SeckillGoods_"+time).put(id,goods);
            }

            return true;
        }
        return false;
    }
}
*/
