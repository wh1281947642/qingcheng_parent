package com.qingcheng.service.impl;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.qingcheng.dao.CategoryBrandMapper;
import com.qingcheng.dao.CategoryMapper;
import com.qingcheng.dao.SkuMapper;
import com.qingcheng.dao.SpuMapper;
import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.goods.*;
import com.qingcheng.service.goods.SpuService;
import com.qingcheng.util.IdWorker;
import com.sun.org.apache.xerces.internal.util.SynchronizedSymbolTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.*;

/**
 * <p>
 * <code>SpuServiceImpl</code>
 * </p>
 * 
 * @author huiwang45@iflytek.com
 * @description
 * @date 2020/03/26 20:22
 */
@Service(interfaceClass = SpuService.class)
public class SpuServiceImpl implements SpuService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private CategoryBrandMapper categoryBrandMapper;

    @Autowired
    private IdWorker idWorker;



    /**
     * 返回全部记录
     * @return
     */
    @Override
    public List<Spu> findAll() {
        return spuMapper.selectAll();
    }

    /**
     * 分页查询
     * @param page 页码
     * @param size 每页记录数
     * @return 分页结果
     */
    @Override
    public PageResult<Spu> findPage(int page, int size) {
        PageHelper.startPage(page,size);
        Page<Spu> spus = (Page<Spu>) spuMapper.selectAll();
        return new PageResult<Spu>(spus.getTotal(),spus.getResult());
    }

    /**
     * 条件查询
     * @param searchMap 查询条件
     * @return
     */
    @Override
    public List<Spu> findList(Map<String, Object> searchMap) {
        Example example = createExample(searchMap);
        return spuMapper.selectByExample(example);
    }

    /**
     * 分页+条件查询
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageResult<Spu> findPage(Map<String, Object> searchMap, int page, int size) {
        PageHelper.startPage(page,size);
        Example example = createExample(searchMap);
        Page<Spu> spus = (Page<Spu>) spuMapper.selectByExample(example);
        return new PageResult<Spu>(spus.getTotal(),spus.getResult());
    }

    /**
     * 根据Id查询
     * @param id
     * @return
     */
    @Override
    public Spu findById(String id) {
        return spuMapper.selectByPrimaryKey(id);
    }

    /**
     * 新增
     * @param spu
     */
    @Override
    public void add(Spu spu) {
        spuMapper.insert(spu);
    }

    /**
     * 修改
     * @param spu
     */
    @Override
    public void update(Spu spu) {
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /**
     *  删除
     * @param id
     */
    @Override
    public void delete(String id) {
        spuMapper.deleteByPrimaryKey(id);
    }

    /**
     * 保存商品（spu+List<sku>,一对多）
     * @description
     * @author huiwang45@iflytek.com
     * @date 2020/03/26 17:13
     * @param goods
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class )
    public void saveGoods(Goods goods) {

        Date date = new Date();

        //保存spu
        Spu spu = goods.getSpu();
        //手动设置id 策略为雪花算法
        long id = idWorker.nextId();
        System.out.println(id);
        spu.setId(id+"");
        spuMapper.insertSelective(spu);

        //商品分类
        Category category = categoryMapper.selectByPrimaryKey(spu.getCategory3Id());

        //保存sku列表
        List<Sku> skuList = goods.getSkuList();
        skuList.forEach(sku -> {
            //手动设置id 策略为雪花算法
            sku.setId(String.valueOf(idWorker.nextId()));
            //设置外键 spuId
            sku.setSpuId(spu.getId());
            //sku名称 = spu名称+规格值列表
            String name = spu.getName();
            //获取规格参数
            String spec = sku.getSpec();
            if(!org.springframework.util.StringUtils.isEmpty(spec)){
                Map<String,String> specMap = JSON.parseObject(spec, Map.class);
                //遍历map
                //方法1 用迭代器
                Set<Map.Entry<String, String>> entrySet = specMap.entrySet();

            /*Iterator<Map.Entry<String, String>> iterator = entrySet.iterator();
            while (iterator.hasNext()){
                Map.Entry<String, String> entry = iterator.next();
                name= name + " " + entry.getValue();
            }*/

                //方法2 增强for循环
                for (Map.Entry<String, String> entry : entrySet) {
                    name= name + " " + entry.getValue();
                }
            }

            //sku名称
            sku.setName(name);
            //创建日期
            sku.setCreateTime(date);
            //修改日期
            sku.setUpdateTime(date);
            //商品分类id
            sku.setCategoryId(spu.getCategory3Id());
            //商品分类名称
            sku.setCategoryName(category.getName());
            //评论数 默认为0
            sku.setCommentNum(0);
            //销售数量 默认为0
            sku.setSaleNum(0);
            skuMapper.insertSelective(sku);
        });

        //建立分类与品牌的关联
        CategoryBrand categoryBrand = new CategoryBrand();
        //分类id
        categoryBrand.setCategoryId(spu.getCategory3Id());
        //品牌id
        categoryBrand.setBrandId(spu.getBrandId());
        //先统计是否已经存在该数据
        int count = categoryBrandMapper.selectCount(categoryBrand);
        if (count == 0){
            categoryBrandMapper.insertSelective(categoryBrand);
        }
    }

    /**
     * 根据spuId查询Goods
     * @description
     * @author huiwang45@iflytek.com
     * @date 2020/03/30 15:24
     * @param
     * @return
     */
    @Override
    public Goods findGoodsById(String id){

        //查询spu
        Spu spu = this.spuMapper.selectByPrimaryKey(id);

        //查询sku列表
        Sku sku = new Sku();
        sku.setSpuId(id);
        List<Sku> skuList = this.skuMapper.select(sku);
        //封装为组合实体类Goods
        Goods goods = new Goods();
        goods.setSpu(spu);
        goods.setSkuList(skuList);

        return goods;
    }

    /**
     * 修改商品
     * @description
     * @author huiwang45@iflytek.com
     * @date 2020/03/30 16:37
     * @param
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class )
    public void updateGoods(Goods goods){
        Date date = new Date();
        Spu spu = goods.getSpu();
        //商品分类
        Category category = categoryMapper.selectByPrimaryKey(spu.getCategory3Id());
        //先删除skuList
        Sku sku = new Sku();
        sku.setSpuId(spu.getId());
        List<Sku> skuListOld = this.skuMapper.select(sku);
        skuListOld.forEach(sku1 -> {
            this.skuMapper.delete(sku1);
        });

        //修改spu
        this.spuMapper.updateByPrimaryKeySelective(spu);

        //修改skuList
        List<Sku> skuList = goods.getSkuList();
        skuList.forEach(sku2 -> {
            //设置外键 spuId
            sku2.setSpuId(spu.getId());
            //sku名称 = spu名称+规格值列表
            String name = spu.getName();
            //获取规格参数
            String spec = sku2.getSpec();
            if(!org.springframework.util.StringUtils.isEmpty(spec)){
                Map<String,String> specMap = JSON.parseObject(spec, Map.class);
                //遍历map
                //方法1 用迭代器
                Set<Map.Entry<String, String>> entrySet = specMap.entrySet();

            /*Iterator<Map.Entry<String, String>> iterator = entrySet.iterator();
            while (iterator.hasNext()){
                Map.Entry<String, String> entry = iterator.next();
                name= name + " " + entry.getValue();
            }*/

                //方法2 增强for循环
                for (Map.Entry<String, String> entry : entrySet) {
                    name= name + " " + entry.getValue();
                }
            }

            //sku名称
            sku2.setName(name);
            //修改日期
            sku2.setUpdateTime(date);
            //商品分类id
            sku2.setCategoryId(spu.getCategory3Id());
            //商品分类名称
            sku2.setCategoryName(category.getName());
            //评论数 默认为0
            sku2.setCommentNum(0);
            //销售数量 默认为0
            sku2.setSaleNum(0);
            skuMapper.insertSelective(sku2);
        });

        //建立分类与品牌的关联
        CategoryBrand categoryBrand = new CategoryBrand();
        //分类id
        categoryBrand.setCategoryId(spu.getCategory3Id());
        //品牌id
        categoryBrand.setBrandId(spu.getBrandId());
        //先统计是否已经存在该数据
        int count = categoryBrandMapper.selectCount(categoryBrand);
        if (count == 0){
            categoryBrandMapper.insertSelective(categoryBrand);
        }
    }


    /**
     * 商品审核
     * @description
     * @author huiwang45@iflytek.com
     * @date 2020/03/31 10:22
     * @param  id 商品id
     * @param  status 状态
     * @param  message
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class )
    public void audit(String id, String status, String message){

        // 1.修改状态 审核状态和上架状态
        //Spu spu = this.spuMapper.selectByPrimaryKey(id);
        Spu spu = new Spu();
        spu.setId(id);
        spu.setStatus(status);
        //审核通过 需要在设置上下架状态
        if ("1".equals(status)){
            //自动上架
            spu.setIsMarketable("1");
        }
        //根据主键修改
        this.spuMapper.updateByPrimaryKeySelective(spu);

        // 2.记录商品审核记录

        // 3.记录商品日志
    }

    /**
     * 商品下架
     * @description
     * @author huiwang45@iflytek.com
     * @date 2020/03/31 10:41
     * @param id spuId
     * @return
     */
    @Override
    public void pull(String id){

        // 1.修改状态
        //Spu spu = this.spuMapper.selectByPrimaryKey(id);
        Spu spu = new Spu();
        spu.setId(id);
        spu.setIsMarketable("0");
        this.spuMapper.updateByPrimaryKeySelective(spu);
        // 2.记录商品日志
    }

    /**
     * 商品上架
     * @description
     * @author huiwang45@iflytek.com
     * @date 2020/03/31 10:41
     * @param id spuId
     * @return
     */
    @Override
    public void put(String id){
        // 1.修改状态
        Spu spu = this.spuMapper.selectByPrimaryKey(id);
        //验证商品是否审核过
        if ( ! "1".equals(spu.getStatus())){
            throw new RuntimeException("此商品未通过审核！");
        }
        spu.setIsMarketable("1");
        this.spuMapper.updateByPrimaryKeySelective(spu);
        // 2.记录商品日志
    }

    /**
     * 批量上架
     * @description
     * @author huiwang45@iflytek.com
     * @date 2020/03/31 10:58
     * @param
     * @return
     */
    @Override
    public int putMany(String [] ids){
        // 1.修改状态
        int num = 0;
        for (String id : ids) {
            Spu spu = this.spuMapper.selectByPrimaryKey(id);
            //审核过且未上架
            if ("1".equals(spu.getStatus()) && "0".equals(spu.getIsMarketable()) ){
                spu.setIsMarketable("1");
                this.spuMapper.updateByPrimaryKeySelective(spu);
                num++;
            }
        }

        // 2.添加商品日志
        return  num;
    }

    /**
     * 构建查询条件
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap){
        Example example=new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        if(searchMap!=null){
            // 主键
            if(searchMap.get("id")!=null && !"".equals(searchMap.get("id"))){
                criteria.andLike("id","%"+searchMap.get("id")+"%");
            }
            // 货号
            if(searchMap.get("sn")!=null && !"".equals(searchMap.get("sn"))){
                criteria.andLike("sn","%"+searchMap.get("sn")+"%");
            }
            // SPU名
            if(searchMap.get("name")!=null && !"".equals(searchMap.get("name"))){
                criteria.andLike("name","%"+searchMap.get("name")+"%");
            }
            // 副标题
            if(searchMap.get("caption")!=null && !"".equals(searchMap.get("caption"))){
                criteria.andLike("caption","%"+searchMap.get("caption")+"%");
            }
            // 图片
            if(searchMap.get("image")!=null && !"".equals(searchMap.get("image"))){
                criteria.andLike("image","%"+searchMap.get("image")+"%");
            }
            // 图片列表
            if(searchMap.get("images")!=null && !"".equals(searchMap.get("images"))){
                criteria.andLike("images","%"+searchMap.get("images")+"%");
            }
            // 售后服务
            if(searchMap.get("saleService")!=null && !"".equals(searchMap.get("saleService"))){
                criteria.andLike("saleService","%"+searchMap.get("saleService")+"%");
            }
            // 介绍
            if(searchMap.get("introduction")!=null && !"".equals(searchMap.get("introduction"))){
                criteria.andLike("introduction","%"+searchMap.get("introduction")+"%");
            }
            // 规格列表
            if(searchMap.get("specItems")!=null && !"".equals(searchMap.get("specItems"))){
                criteria.andLike("specItems","%"+searchMap.get("specItems")+"%");
            }
            // 参数列表
            if(searchMap.get("paraItems")!=null && !"".equals(searchMap.get("paraItems"))){
                criteria.andLike("paraItems","%"+searchMap.get("paraItems")+"%");
            }
            // 是否上架
            if(searchMap.get("isMarketable")!=null && !"".equals(searchMap.get("isMarketable"))){
                criteria.andLike("isMarketable","%"+searchMap.get("isMarketable")+"%");
            }
            // 是否启用规格
            if(searchMap.get("isEnableSpec")!=null && !"".equals(searchMap.get("isEnableSpec"))){
                criteria.andLike("isEnableSpec","%"+searchMap.get("isEnableSpec")+"%");
            }
            // 是否删除
            if(searchMap.get("isDelete")!=null && !"".equals(searchMap.get("isDelete"))){
                criteria.andLike("isDelete","%"+searchMap.get("isDelete")+"%");
            }
            // 审核状态
            if(searchMap.get("status")!=null && !"".equals(searchMap.get("status"))){
                criteria.andLike("status","%"+searchMap.get("status")+"%");
            }

            // 品牌ID
            if(searchMap.get("brandId")!=null ){
                criteria.andEqualTo("brandId",searchMap.get("brandId"));
            }
            // 一级分类
            if(searchMap.get("category1Id")!=null ){
                criteria.andEqualTo("category1Id",searchMap.get("category1Id"));
            }
            // 二级分类
            if(searchMap.get("category2Id")!=null ){
                criteria.andEqualTo("category2Id",searchMap.get("category2Id"));
            }
            // 三级分类
            if(searchMap.get("category3Id")!=null ){
                criteria.andEqualTo("category3Id",searchMap.get("category3Id"));
            }
            // 模板ID
            if(searchMap.get("templateId")!=null ){
                criteria.andEqualTo("templateId",searchMap.get("templateId"));
            }
            // 运费模板id
            if(searchMap.get("freightId")!=null ){
                criteria.andEqualTo("freightId",searchMap.get("freightId"));
            }
            // 销量
            if(searchMap.get("saleNum")!=null ){
                criteria.andEqualTo("saleNum",searchMap.get("saleNum"));
            }
            // 评论数
            if(searchMap.get("commentNum")!=null ){
                criteria.andEqualTo("commentNum",searchMap.get("commentNum"));
            }

        }
        return example;
    }
}
