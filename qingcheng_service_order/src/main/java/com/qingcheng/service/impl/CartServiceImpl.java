package com.qingcheng.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.qingcheng.pojo.goods.Category;
import com.qingcheng.pojo.goods.Sku;
import com.qingcheng.pojo.order.OrderItem;
import com.qingcheng.service.goods.CategoryService;
import com.qingcheng.service.goods.SkuService;
import com.qingcheng.service.order.CartService;
import com.qingcheng.util.CacheKey;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;

import java.text.BreakIterator;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <p>
 * <code>CartServiceImpl</code>
 * </p>
 * 
 * @author huiwang45@iflytek.com
 * @description
 * @date 2020/07/03 15:13
 */
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Reference
    private SkuService skuService;

    @Reference
    private CategoryService categoryService;


    @Override
    public List<Map<String, Object>> findCartList(String username) {
        System.out.println("从redis中提取购物车"+username);
        List<Map<String, Object>>  cartList = (List<Map<String, Object>>)redisTemplate.boundHashOps(CacheKey.CART_LIST).get(username);
        if (CollectionUtils.isEmpty(cartList)){
            cartList = new ArrayList<>();
        }
        return cartList;
    }



    @Override
    public void addItem(String username, String skuId, Integer num) {
       //实现思路: 遍历购物车，如果购物车中存在该商品则累加，如果不存在则添加购物车

        //获取购物车
        List<Map<String, Object>> cartList = this.findCartList(username);
        //是否在购物车中存在
        //AtomicBoolean flag = new AtomicBoolean(false);
        boolean flag = false;
        /*cartList.forEach(cart->{
            OrderItem orderItem = (OrderItem)cart.get("item");
            //如果购物车中存在该商品
            if (orderItem.getSkuId().equals(skuId)){
                flag.set(true);
                break;
            }
        });*/

        for (Map<String, Object> map : cartList) {
            OrderItem orderItem = (OrderItem)map.get("item");
            //如果购物车中存在该商品
            if (orderItem.getSkuId().equals(skuId)){
                if (orderItem.getNum()<=0){
                    cartList.remove(map);
                    flag = true;
                    break;
                }
                //单个商品的重量
                int weight = orderItem.getWeight()/orderItem.getNum();
                //数量的变更
                orderItem.setNum(orderItem.getNum()+num);
                //金额的变更
                orderItem.setMoney(orderItem.getPrice()*orderItem.getNum());
                //重量的变更
                orderItem.setWeight(weight*orderItem.getNum());
                if (orderItem.getNum()<=0){
                    cartList.remove(map);
                }
                flag = true;
                break;
            }
        }

        //购物车中不存在改商品
        if (flag = false){
            Sku sku = this.skuService.findById(skuId);
            if (sku == null){
                throw new RuntimeException("商品不存在");
            }
            if (!"1".equals(sku.getStatus())){
                throw new RuntimeException("商品状态不合法");
            }
            if (num<=0){
                throw new RuntimeException("商品数量不合法");
            }

            OrderItem orderItem = new OrderItem();
            BeanUtils.copyProperties(sku, orderItem);
            orderItem.setSkuId(skuId);
            //orderItem.setSpuId(orderItem.getSpuId());
            orderItem.setNum(num);
            //orderItem.setImage(sku.getImage());
            //orderItem.setPrice(sku.getPrice());
            //orderItem.setName(sku.getName());
            //金额的计算
            orderItem.setMoney(sku.getPrice()*num);
            if (sku.getWeight() == null){
                sku.setWeight(0);
            }
            //重量的计算
            orderItem.setWeight(sku.getWeight()*num);

            //商品分类
            //三级分类id
            orderItem.setCategoryId3(sku.getCategoryId());
            Category category3 = (Category)redisTemplate.boundHashOps(CacheKey.CATEGROY).get(sku.getCategoryId());
            if (category3 == null){
                //通过三级分类找二级分类（ParentId）
                category3 = this.categoryService.findById(sku.getCategoryId());
                redisTemplate.boundHashOps(CacheKey.CATEGROY).put(sku.getCategoryId(), category3);
            }
            //二级分类
            orderItem.setCategoryId2(category3.getParentId());

            Category category2 = (Category)redisTemplate.boundHashOps(CacheKey.CATEGROY).get(category3.getParentId());
            if (category2 == null){
                //通过二级分类找一级分类（ParentId）
                category2 = this.categoryService.findById(category3.getParentId());
                redisTemplate.boundHashOps(CacheKey.CATEGROY).put(category3.getParentId(), category3);
            }
            //一级分类
            orderItem.setCategoryId1(category2.getParentId());

            HashMap<String, Object> map = new HashMap<>();
            map.put("item",orderItem );
            //默认选中
            map.put("checked",true );
            cartList.add(map);
        }

        //覆盖之前的信息
        redisTemplate.boundHashOps(CacheKey.CART_LIST).put(username, cartList);
    }



    /*
    @Override
    public boolean updateChecked(String username, String skuId, boolean checked) {

        List<Map<String, Object>> cartList = findCartList(username);
        boolean isOk=false;
        for( Map map:cartList){
            OrderItem orderItem=(OrderItem)map.get("item");
            if( orderItem.getSkuId().equals( skuId)){
                map.put("checked",checked);
                isOk=true;
                break;
            }
        }
        if(isOk){
            redisTemplate.boundHashOps(CacheKey.CART_LIST).put(username,cartList);
        }
        return isOk;
    }

    @Override
    public void deleteCheckedCart(String username) {
        //获得未选中的购物车
        List<Map<String, Object>> cartList = findCartList(username).stream().filter(cart -> (boolean) cart.get("checked") == false)
                .collect(Collectors.toList());
        redisTemplate.boundHashOps(CacheKey.CART_LIST).put(username,cartList);
    }

    @Autowired
    private PreferentialService preferentialService;

    @Override
    public int preferential(String username) {

        //获取选中的购物车  List<OrderItem>  List<Map>
        List<OrderItem> orderItemList = findCartList(username).stream()
                .filter(cart -> (boolean) cart.get("checked") == true)
                .map(cart -> (OrderItem) cart.get("item"))
                .collect(Collectors.toList());

        //按分类聚合统计每个分类的金额    group by
        Map<Integer, IntSummaryStatistics> cartMap = orderItemList.stream()
                .collect(Collectors.groupingBy(OrderItem::getCategoryId3, Collectors.summarizingInt(OrderItem::getMoney)));
        //循环结果，统计每个分类的优惠金额，并累加

        int allPreMoney=0;//累计优惠金额
        for( Integer categoryId:  cartMap.keySet() ){
            // 获取品类的消费金额
            int money = (int)cartMap.get(categoryId).getSum();
            int preMoney = preferentialService.findPreMoneyByCategoryId(categoryId, money); //获取优惠金额
            System.out.println("分类："+categoryId+"  消费金额："+ money+  " 优惠金额：" + preMoney );

            allPreMoney+=   preMoney;
        }

        return allPreMoney;
    }*/

}
