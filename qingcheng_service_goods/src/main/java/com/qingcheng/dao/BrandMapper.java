package com.qingcheng.dao;

import com.qingcheng.pojo.goods.Brand;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

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
public interface BrandMapper extends Mapper<Brand> {

    /**
     * 根据商品分类名称查询品牌列表
     * @description
     * @author huiwang45@iflytek.com
     * @date 2020/05/29 17:23
     * @param
     * @return 
     */
    @Select("SELECT b.name,b.image " +
            "FROM tb_brand b " +
            "LEFT JOIN tb_category_brand cb ON cb.brand_id = b.id " +
            "LEFT JOIN tb_category c ON c.id = cb.category_id " +
            "WHERE c.name =#{name} ORDER BY b.seq")
    public List<Map> findListByCategoryName(@Param("name") String categoryName);
}
