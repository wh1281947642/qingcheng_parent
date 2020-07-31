package com.qingcheng.service.impl;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.qingcheng.dao.OrderConfigMapper;
import com.qingcheng.dao.OrderItemMapper;
import com.qingcheng.dao.OrderLogMapper;
import com.qingcheng.dao.OrderMapper;
import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.order.Order;
import com.qingcheng.pojo.order.OrderConfig;
import com.qingcheng.pojo.order.OrderItem;
import com.qingcheng.pojo.order.OrderLog;
import com.qingcheng.service.goods.SkuService;
import com.qingcheng.service.order.CartService;
import com.qingcheng.service.order.OrderService;
import com.qingcheng.util.IdWorker;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import javax.swing.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service(interfaceClass = OrderService.class)
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderConfigMapper orderConfigMapper;

    @Autowired
    private OrderLogMapper orderLogMapper;

    @Autowired
    private CartService cartService;

    @Reference
    private SkuService skuService;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;
    /**
     * 返回全部记录
     * @return
     */
    @Override
    public List<Order> findAll() {
        return orderMapper.selectAll();
    }

    /**
     * 分页查询
     * @param page 页码
     * @param size 每页记录数
     * @return 分页结果
     */
    @Override
    public PageResult<Order> findPage(int page, int size) {
        PageHelper.startPage(page,size);
        Page<Order> orders = (Page<Order>) orderMapper.selectAll();
        return new PageResult<Order>(orders.getTotal(),orders.getResult());
    }

    /**
     * 条件查询
     * @param searchMap 查询条件
     * @return
     */
    @Override
    public List<Order> findList(Map<String, Object> searchMap) {
        Example example = createExample(searchMap);
        return orderMapper.selectByExample(example);
    }

    /**
     * 分页+条件查询
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageResult<Order> findPage(Map<String, Object> searchMap, int page, int size) {
        PageHelper.startPage(page,size);
        Example example = createExample(searchMap);
        Page<Order> orders = (Page<Order>) orderMapper.selectByExample(example);
        return new PageResult<Order>(orders.getTotal(),orders.getResult());
    }

    /**
     * 根据Id查询
     * @param id
     * @return
     */
    @Override
    public Order findById(String id) {
        return orderMapper.selectByPrimaryKey(id);
    }

    /**
     * 新增
     * @param order
     */
    @Override
    public Map<String,Object> add(Order order) {

        //1.获取选中的购物车
        List<Map<String, Object>> orderItemList = this.cartService.findNewOrderItemList(order.getUsername());
        List<OrderItem> itemList = orderItemList.stream().filter(map -> (Boolean) map.get("checked") == true).map(map -> (OrderItem) map.get("item")).collect(Collectors.toList());

        //2.扣减库存
        if (!this.skuService.deductionStock(itemList)){
           //扣减库存失败
            throw new RuntimeException("库存不足!");
        }

        try {
            //3.保存订单主表
            order.setId(String.valueOf(idWorker.nextId()));
            //总数
            IntStream numStream = itemList.stream().mapToInt(OrderItem::getNum);
            int totalNum = numStream.sum();
            order.setTotalNum(totalNum);
            //总价钱
            IntStream moneyStream = itemList.stream().mapToInt(OrderItem::getMoney);
            int totalMoney = moneyStream.sum();
            order.setTotalMoney(totalMoney);
            //计算满减优惠金额
            int preMoney = this.cartService.preferential(order.getUsername());
            order.setPreMoney(preMoney);
            //支付金额
            order.setPayMoney(totalMoney-preMoney);
            //订单的创建时间
            order.setCreateTime(new Date());
            //订单状态
            order.setOrderStatus("0");
            //支付状态
            order.setPayStatus("0");
            //发货状态
            order.setConsignStatus("0");
            this.orderMapper.insertSelective(order);

            //4.保存订单明细表
            //打折比例
            double proportion = (double)order.getPayMoney()/totalMoney;
            for (OrderItem orderItem : itemList) {
                orderItem.setId(String.valueOf(idWorker.nextId()));
                orderItem.setOrderId(order.getId());
                orderItem.setPayMoney((int)(orderItem.getMoney()*proportion));
                this.orderItemMapper.insertSelective(orderItem);
            }
        } catch (Exception e) {
            e.printStackTrace();
            //发送回滚消息
            rabbitTemplate.convertAndSend("", "skuback", JSON.toJSONString(itemList));
            throw  new  RuntimeException("创建订单失败!");
        }

        //5.清楚购物车
        cartService.deleteCheckedCart(order.getUsername());

        //6.封装返回结果集
        HashMap<String, Object> hashMap = new HashMap<>();
        //订单号
        hashMap.put("ordersn", order.getId());
        System.out.println("order.getId():"+order.getId());
        //实际支付的金额
        hashMap.put("money", order.getPayMoney());

        return  hashMap;
    }

    /**
     * 修改
     * @param order
     */
    @Override
    public void update(Order order) {
        orderMapper.updateByPrimaryKeySelective(order);
    }

    /**
     *  删除
     * @param id
     */
    @Override
    public void delete(String id) {
        orderMapper.deleteByPrimaryKey(id);
    }


    /**
     * 批量发货
     * @description
     * @author huiwang45@iflytek.com
     * @date 2020/04/01 16:17
     * @param
     * @return
     */
    @Override
    public void batchSend(List<Order> orders) {
        //判断运单号和物流公司是否为空
        for(Order order :orders){
            if(order.getShippingCode()==null || order.getShippingName()==null){
                throw new RuntimeException("请选择快递公司和填写快递单号");
            }
        }
        //循环订单
        for(Order order :orders){
            order.setOrderStatus("3");//订单状态  已发货
            order.setConsignStatus("2");//发货状态  已发货
            order.setConsignTime(new Date());//发货时间
            orderMapper.updateByPrimaryKeySelective(order);
            //记录订单日志  。。。（代码略）
        }
    }

    /**
     * 订单超时处理
     * @description
     * @author huiwang45@iflytek.com
     * @date 2020/04/03 14:53
     * @param
     * @return
     */
    @Override
    public void orderTimeOutLogic() {
        //订单超时未付款 自动关闭
        //查询超时时间
        OrderConfig orderConfig = orderConfigMapper.selectByPrimaryKey(1);
        //超时时间（分）60
        Integer orderTimeout = orderConfig.getOrderTimeout();
        //得到超时的时间点
        LocalDateTime localDateTime = LocalDateTime.now().minusMinutes(orderTimeout);
        //设置查询条件
        Example example=new Example(Order.class);
        Example.Criteria criteria = example.createCriteria();
        //创建时间小于超时时间
        criteria.andLessThan("createTime",localDateTime);
        //未付款的
        criteria.andEqualTo("orderStatus","0");
        //未删除的
        criteria.andEqualTo("isDelete","0");
        //查询超时订单
        List<Order> orders = orderMapper.selectByExample(example);
        for(Order order :orders){
            //记录订单变动日志
            OrderLog orderLog=new OrderLog();
            // 系统
            orderLog.setOperater("system");
            //当前日期
            orderLog.setOperateTime(new Date());
            orderLog.setOrderStatus("4");
            orderLog.setPayStatus(order.getPayStatus());
            orderLog.setConsignStatus(order.getConsignStatus());
            orderLog.setRemarks("超时订单，系统自动关闭");
            orderLog.setOrderId(order.getId());
            orderLogMapper.insert(orderLog);
            //更改订单状态
            order.setOrderStatus("4");
            //关闭日期
            order.setCloseTime(new Date());
            orderMapper.updateByPrimaryKeySelective(order);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePayStatus(String orderId, String transactionId) {
        System.out.println("调用修改订单状态");
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if(order!=null && "0".equals( order.getPayStatus() )){
            //修改订单状态等信息
            //支付状态
            order.setPayStatus("1");
            //订单状态
            order.setOrderStatus("1");
            //修改日期
            order.setUpdateTime(new Date());
            //支付日期
            order.setPayTime(new Date());
            //交易流水号
            order.setTransactionId(transactionId);
            //修改
            orderMapper.updateByPrimaryKeySelective(order);

            //记录订单日志
            OrderLog orderLog=new OrderLog();
            orderLog.setId( idWorker.nextId()+"" );
            //系统
            orderLog.setOperater("system");
            //操作时间
            orderLog.setOperateTime(new Date());
            //订单状态
            orderLog.setOrderStatus("1");
            //支付状态
            orderLog.setPayStatus("1");
            //备注
            orderLog.setRemarks("支付流水号："+transactionId);
            orderLog.setOrderId(orderId);
            orderLogMapper.insert(orderLog);
        }
    }

    /**
     * 构建查询条件
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap){
        Example example=new Example(Order.class);
        Example.Criteria criteria = example.createCriteria();
        if(searchMap!=null){
            // 订单id
            if(searchMap.get("id")!=null && !"".equals(searchMap.get("id"))){
                criteria.andLike("id","%"+searchMap.get("id")+"%");
            }
            // 支付类型，1、在线支付、0 货到付款
            if(searchMap.get("payType")!=null && !"".equals(searchMap.get("payType"))){
                criteria.andLike("payType","%"+searchMap.get("payType")+"%");
            }
            // 物流名称
            if(searchMap.get("shippingName")!=null && !"".equals(searchMap.get("shippingName"))){
                criteria.andLike("shippingName","%"+searchMap.get("shippingName")+"%");
            }
            // 物流单号
            if(searchMap.get("shippingCode")!=null && !"".equals(searchMap.get("shippingCode"))){
                criteria.andLike("shippingCode","%"+searchMap.get("shippingCode")+"%");
            }
            // 用户名称
            if(searchMap.get("username")!=null && !"".equals(searchMap.get("username"))){
                criteria.andLike("username","%"+searchMap.get("username")+"%");
            }
            // 买家留言
            if(searchMap.get("buyerMessage")!=null && !"".equals(searchMap.get("buyerMessage"))){
                criteria.andLike("buyerMessage","%"+searchMap.get("buyerMessage")+"%");
            }
            // 是否评价
            if(searchMap.get("buyerRate")!=null && !"".equals(searchMap.get("buyerRate"))){
                criteria.andLike("buyerRate","%"+searchMap.get("buyerRate")+"%");
            }
            // 收货人
            if(searchMap.get("receiverContact")!=null && !"".equals(searchMap.get("receiverContact"))){
                criteria.andLike("receiverContact","%"+searchMap.get("receiverContact")+"%");
            }
            // 收货人手机
            if(searchMap.get("receiverMobile")!=null && !"".equals(searchMap.get("receiverMobile"))){
                criteria.andLike("receiverMobile","%"+searchMap.get("receiverMobile")+"%");
            }
            // 收货人地址
            if(searchMap.get("receiverAddress")!=null && !"".equals(searchMap.get("receiverAddress"))){
                criteria.andLike("receiverAddress","%"+searchMap.get("receiverAddress")+"%");
            }
            // 订单来源：1:web，2：app，3：微信公众号，4：微信小程序  5 H5手机页面
            if(searchMap.get("sourceType")!=null && !"".equals(searchMap.get("sourceType"))){
                criteria.andLike("sourceType","%"+searchMap.get("sourceType")+"%");
            }
            // 交易流水号
            if(searchMap.get("transactionId")!=null && !"".equals(searchMap.get("transactionId"))){
                criteria.andLike("transactionId","%"+searchMap.get("transactionId")+"%");
            }
            // 订单状态
            if(searchMap.get("orderStatus")!=null && !"".equals(searchMap.get("orderStatus"))){
                criteria.andLike("orderStatus","%"+searchMap.get("orderStatus")+"%");
            }
            // 支付状态
            if(searchMap.get("payStatus")!=null && !"".equals(searchMap.get("payStatus"))){
                criteria.andLike("payStatus","%"+searchMap.get("payStatus")+"%");
            }
            // 发货状态
            if(searchMap.get("consignStatus")!=null && !"".equals(searchMap.get("consignStatus"))){
                criteria.andLike("consignStatus","%"+searchMap.get("consignStatus")+"%");
            }
            // 是否删除
            if(searchMap.get("isDelete")!=null && !"".equals(searchMap.get("isDelete"))){
                criteria.andLike("isDelete","%"+searchMap.get("isDelete")+"%");
            }

            // 数量合计
            if(searchMap.get("totalNum")!=null ){
                criteria.andEqualTo("totalNum",searchMap.get("totalNum"));
            }
            // 金额合计
            if(searchMap.get("totalMoney")!=null ){
                criteria.andEqualTo("totalMoney",searchMap.get("totalMoney"));
            }
            // 优惠金额
            if(searchMap.get("preMoney")!=null ){
                criteria.andEqualTo("preMoney",searchMap.get("preMoney"));
            }
            // 邮费
            if(searchMap.get("postFee")!=null ){
                criteria.andEqualTo("postFee",searchMap.get("postFee"));
            }
            // 实付金额
            if(searchMap.get("payMoney")!=null ){
                criteria.andEqualTo("payMoney",searchMap.get("payMoney"));
            }
            // 根据  id 数组查询 查询
            if(searchMap.get("ids")!=null ){
                criteria.andIn("id", Arrays.asList((String[])searchMap.get("ids")));
            }
        }
        return example;
    }

}
