package com.qingcheng.service.impl;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.qingcheng.dao.PreferentialMapper;
import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.order.Preferential;
import com.qingcheng.service.order.PreferentialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class PreferentialServiceImpl implements PreferentialService {

    @Autowired
    private PreferentialMapper preferentialMapper;

    /**
     * 返回全部记录
     * @return
     */
    @Override
    public List<Preferential> findAll() {
        return preferentialMapper.selectAll();
    }

    /**
     * 分页查询
     * @param page 页码
     * @param size 每页记录数
     * @return 分页结果
     */
    @Override
    public PageResult<Preferential> findPage(int page, int size) {
        PageHelper.startPage(page,size);
        Page<Preferential> preferentials = (Page<Preferential>) preferentialMapper.selectAll();
        return new PageResult<Preferential>(preferentials.getTotal(),preferentials.getResult());
    }

    /**
     * 条件查询
     * @param searchMap 查询条件
     * @return
     */
    @Override
    public List<Preferential> findList(Map<String, Object> searchMap) {
        Example example = createExample(searchMap);
        return preferentialMapper.selectByExample(example);
    }

    /**
     * 分页+条件查询
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageResult<Preferential> findPage(Map<String, Object> searchMap, int page, int size) {
        PageHelper.startPage(page,size);
        Example example = createExample(searchMap);
        Page<Preferential> preferentials = (Page<Preferential>) preferentialMapper.selectByExample(example);
        return new PageResult<Preferential>(preferentials.getTotal(),preferentials.getResult());
    }

    /**
     * 根据Id查询
     * @param id
     * @return
     */
    @Override
    public Preferential findById(Integer id) {
        return preferentialMapper.selectByPrimaryKey(id);
    }

    /**
     * 新增
     * @param preferential
     */
    @Override
    public void add(Preferential preferential) {
        preferentialMapper.insert(preferential);
    }

    /**
     * 修改
     * @param preferential
     */
    @Override
    public void update(Preferential preferential) {
        preferentialMapper.updateByPrimaryKeySelective(preferential);
    }

    /**
     *  删除
     * @param id
     */
    @Override
    public void delete(Integer id) {
        preferentialMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int findPreMoneyByCategoryId(Integer categoryId, int money) {
        //指定查询条件在优惠规格计算表中查询

        //查询条件: 状态1 分类: 消费额 开始时间和结束时间 排序: 消费额降序

        // 200减40 慢150减20

        Example example=new Example(Preferential.class);
        Example.Criteria criteria = example.createCriteria();
        //状态
        criteria.andEqualTo("state","1");
        //分类
        criteria.andEqualTo("categoryId",categoryId);
        //消费额
        criteria.andLessThanOrEqualTo("buyMoney",money);
        //截止日期大于等于当前日期
        criteria.andGreaterThanOrEqualTo("endTime", new Date());
        //开始日期小于等于当前日期
        criteria.andLessThanOrEqualTo("startTime",new Date());
        //按购买金额降序排列
        example.setOrderByClause("buy_money desc");

        List<Preferential> preferentialList = preferentialMapper.selectByExample(example);
        //有记录（有优惠）
        if (!CollectionUtils.isEmpty(preferentialList)){
            Preferential preferential = preferentialList.get(0);
            //如果不翻倍
            if ("1".equals(preferential.getType())){
                return preferential.getPreMoney();
            }else {
                //翻倍
                //计算倍数
               int multiple =  money/ preferential.getBuyMoney();
               return preferential.getPreMoney()*multiple;
            }
        }else {
            //没有优惠
            return 0;
        }
    }

    /**
     * 构建查询条件
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap){
        Example example=new Example(Preferential.class);
        Example.Criteria criteria = example.createCriteria();
        if(searchMap!=null){
            // 状态
            if(searchMap.get("state")!=null && !"".equals(searchMap.get("state"))){
                criteria.andLike("state","%"+searchMap.get("state")+"%");
            }
            // 类型1不翻倍 2翻倍
            if(searchMap.get("type")!=null && !"".equals(searchMap.get("type"))){
                criteria.andLike("type","%"+searchMap.get("type")+"%");
            }

            // ID
            if(searchMap.get("id")!=null ){
                criteria.andEqualTo("id",searchMap.get("id"));
            }
            // 消费金额
            if(searchMap.get("buyMoney")!=null ){
                criteria.andEqualTo("buyMoney",searchMap.get("buyMoney"));
            }
            // 优惠金额
            if(searchMap.get("preMoney")!=null ){
                criteria.andEqualTo("preMoney",searchMap.get("preMoney"));
            }

        }
        return example;
    }

}
