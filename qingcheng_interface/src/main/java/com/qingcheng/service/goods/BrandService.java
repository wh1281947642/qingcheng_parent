package com.qingcheng.service.goods;

import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.goods.Brand;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * <code>BrandServiceImpl</code>
 * </p>
 * 品牌管理
 * @author huiwang45@iflytek.com
 * @description
 * @date 2020/03/11 17:20
 */
public interface BrandService {

    String test();

    /**
     * 查询全部
     * @description
     * @author huiwang45@iflytek.com
     * @date 2020/03/12 15:21
     * @param
     * @return
     */
    public List<Brand> findAll();

    /**
     * 分页查询
     * @description
     * @author huiwang45@iflytek.com
     * @date 2020/03/12 15:21
     * @param
     * @return
     */
    public PageResult<Brand> findPage(int page,int size);

    /**
     * example 条件查询
     * @description
     * @author huiwang45@iflytek.com
     * @date 2020/03/12 15:33
     * @param
     * @return
     */
    public List<Brand> findList(Map<String,Object> searchMap);

    /**
     * 条件分页查询
     * @description
     * @author huiwang45@iflytek.com
     * @date 2020/03/12 15:55
     * @param
     * @return
     */
    public PageResult<Brand> findPage(Map<String,Object> searchMap,int page,int size);

    /**
     * 根据ID查询品牌
     * @description
     * @author huiwang45@iflytek.com
     * @date 2020/03/12 16:05
     * @param
     * @return
     */
    public Brand findById(Integer id);

    /**
     * 品牌新增
     * @description
     * @author huiwang45@iflytek.com
     * @date 2020/03/12 16:09
     * @param
     * @return
     */
    public void add(Brand brand);

    /**
     * 品牌修改
     * @description
     * @author huiwang45@iflytek.com
     * @date 2020/03/12 16:18
     * @param
     * @return
     */
    public void update(Brand brand);

    /**
     * 品牌删除
     * @description
     * @author huiwang45@iflytek.com
     * @date 2020/03/12 16:24
     * @param
     * @return
     */
    public void delete(Integer id);
}
