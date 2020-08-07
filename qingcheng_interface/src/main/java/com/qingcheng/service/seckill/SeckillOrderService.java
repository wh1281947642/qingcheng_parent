package com.qingcheng.service.seckill;

/****
 * @Author:itheima
 * @Date:2019/5/27 18:06
 * @Description:
 *****/
public interface SeckillOrderService {


    /****
     * 下单实现
     * @param id:商品ID
     * @param time:商品时区
     * @param username:用户名
     * @return
     */
    Boolean add(Long id, String time, String username);

}
