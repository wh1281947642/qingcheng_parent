package com.qingcheng.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.qingcheng.dao.BrandMapper;
import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.goods.Brand;
import com.qingcheng.service.goods.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

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
@Service
public class BrandServiceImpl implements BrandService {

    @Autowired
    private BrandMapper brandMapper;

    @Override
    public String test() {
        return "BrandService.test";
    }

    /**
     * 查询全部
     * @description
     * @author huiwang45@iflytek.com
     * @date 2020/03/12 15:21
     * @param
     * @return
     */
    @Override
    public List<Brand> findAll() {
        return brandMapper.selectAll();
    }

    /**
     * 分页查询
     * @description
     * @author huiwang45@iflytek.com
     * @date 2020/03/12 15:21
     * @param
     * @return
     */
    @Override
    public PageResult<Brand> findPage(int page, int size) {
        PageHelper.startPage(page,size);
        Page<Brand> pageResult=(Page<Brand>) brandMapper.selectAll();
        return new PageResult<>(pageResult.getTotal(),pageResult.getResult());
    }

    /**
     * example 条件查询
     * @description
     * @author huiwang45@iflytek.com
     * @date 2020/03/12 15:33
     * @param
     * @return
     */
    @Override
    public List<Brand> findList(Map<String, Object> searchMap) {
        Example example = createExample(searchMap);
        return brandMapper.selectByExample(example);
    }

    /**
     * 条件分页查询
     * @description
     * @author huiwang45@iflytek.com
     * @date 2020/03/12 15:55
     * @param
     * @return
     */
    @Override
    public PageResult<Brand> findPage(Map<String, Object> searchMap, int page, int size) {
        PageHelper.startPage(page,size);
        Example example = createExample(searchMap);
        Page<Brand> pageResult=(Page<Brand>) brandMapper.selectByExample(example);
        return new PageResult<>(pageResult.getTotal(),pageResult.getResult());
    }

    /**
     * 根据ID查询品牌
     * @description
     * @author huiwang45@iflytek.com
     * @date 2020/03/12 16:05
     * @param 
     * @return 
     */
    @Override
    public Brand findById(Integer id) {
        return brandMapper.selectByPrimaryKey(id);
    }

    /**
     * 品牌新增
     * @description
     * @author huiwang45@iflytek.com
     * @date 2020/03/12 16:09
     * @param
     * @return
     */
    @Override
    public void add(Brand brand) {
        brandMapper.insertSelective(brand);
    }

    /**
     * 品牌修改
     * @description
     * @author huiwang45@iflytek.com
     * @date 2020/03/12 16:18
     * @param
     * @return
     */
    @Override
    public void update(Brand brand) {
        brandMapper.updateByPrimaryKeySelective(brand);
    }

    /**
     * 品牌删除
     * @description
     * @author huiwang45@iflytek.com
     * @date 2020/03/12 16:24
     * @param
     * @return
     */
    @Override
    public void delete(Integer id) {
        brandMapper.deleteByPrimaryKey(id);
    }

    private Example createExample(Map<String, Object> searchMap){
        Example example=new Example(Brand.class);
        Example.Criteria criteria = example.createCriteria();

        if(! CollectionUtils.isEmpty(searchMap)){

            if(!StringUtils.isEmpty((String)searchMap.get("name")) ){
                criteria.andLike("name","%"+(String)searchMap.get("name")+"%");
            }
            if(!StringUtils.isEmpty((String)searchMap.get("letter"))  ){
                criteria.andEqualTo("letter",(String)searchMap.get("letter"));
            }
        }
        return example;
    }
}
