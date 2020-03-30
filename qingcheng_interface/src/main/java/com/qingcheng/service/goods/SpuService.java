package com.qingcheng.service.goods;
import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.goods.Goods;
import com.qingcheng.pojo.goods.Spu;

import java.util.*;

/**
 * spu业务逻辑层
 */
public interface SpuService {


    public List<Spu> findAll();


    public PageResult<Spu> findPage(int page, int size);


    public List<Spu> findList(Map<String, Object> searchMap);


    public PageResult<Spu> findPage(Map<String, Object> searchMap, int page, int size);


    public Spu findById(String id);

    public void add(Spu spu);


    public void update(Spu spu);

    public void delete(String id);

    /**
     * 保存商品
     * @description
     * @author huiwang45@iflytek.com
     * @date 2020/03/30 15:24
     * @param
     * @return
     */
    public void saveGoods(Goods goods);

    /**
     * 查询商品
     * @description
     * @author huiwang45@iflytek.com
     * @date 2020/03/30 15:24
     * @param id
     * @return Goods
     */
    public Goods findGoodsById(String id);

    /**
     * 修改商品
     * @description
     * @author huiwang45@iflytek.com
     * @date 2020/03/30 16:37
     * @param goods
     * @return
     */
    public void updateGoods(Goods goods);
}
