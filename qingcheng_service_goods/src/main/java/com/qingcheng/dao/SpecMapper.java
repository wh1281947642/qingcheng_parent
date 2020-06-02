package com.qingcheng.dao;

import com.qingcheng.pojo.goods.Spec;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * <code>SpecMapper</code>
 * </p>
 * 
 * @author huiwang45@iflytek.com
 * @description
 * @date 2020/06/02 11:29
 */
public interface SpecMapper extends Mapper<Spec> {

    /**
     * 根据商品分类查询规格列表
     * @description
     * @author huiwang45@iflytek.com
     * @date 2020/06/02 11:30
     * @param
     * @return
     */
    @Select("SELECT DISTINCT tbs.name,tbs.options " +
            "FROM tb_spec tbs " +
            "LEFT JOIN tb_category tbc ON tbc.template_id = tbs.template_id " +
            "WHERE tbc. NAME = #{categoryName}" )
    public List<Map> findListByCategoryName(@Param("categoryName") String categoryName);

}
