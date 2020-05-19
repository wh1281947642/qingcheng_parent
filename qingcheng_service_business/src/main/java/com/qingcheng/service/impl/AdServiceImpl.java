package com.qingcheng.service.impl;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.qingcheng.dao.AdMapper;
import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.business.Ad;
import com.qingcheng.service.business.AdService;
import com.qingcheng.util.CacheKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * <code>AdServiceImpl</code>
 * </p>
 * 
 * @author huiwang45@iflytek.com
 * @description
 * @date 2020/05/19 15:16
 */
@Service
public class AdServiceImpl implements AdService {

    @Autowired
    private AdMapper adMapper;

    @Autowired
    private RedisTemplate redisTemplate;
    
    

    /**
     * 返回全部记录
     * @return
     */
    @Override
    public List<Ad> findAll() {
        return adMapper.selectAll();
    }

    /**
     * 分页查询
     * @param page 页码
     * @param size 每页记录数
     * @return 分页结果
     */
    @Override
    public PageResult<Ad> findPage(int page, int size) {
        PageHelper.startPage(page,size);
        Page<Ad> ads = (Page<Ad>) adMapper.selectAll();
        return new PageResult<Ad>(ads.getTotal(),ads.getResult());
    }

    /**
     * 条件查询
     * @param searchMap 查询条件
     * @return
     */
    @Override
    public List<Ad> findList(Map<String, Object> searchMap) {
        Example example = createExample(searchMap);
        return adMapper.selectByExample(example);
    }

    /**
     * 分页+条件查询
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageResult<Ad> findPage(Map<String, Object> searchMap, int page, int size) {
        PageHelper.startPage(page,size);
        Example example = createExample(searchMap);
        Page<Ad> ads = (Page<Ad>) adMapper.selectByExample(example);
        return new PageResult<Ad>(ads.getTotal(),ads.getResult());
    }

    /**
     * 根据Id查询
     * @param id
     * @return
     */
    @Override
    public Ad findById(Integer id) {
        return adMapper.selectByPrimaryKey(id);
    }

    /**
     * 新增
     * @param ad
     */
    @Override
    public void add(Ad ad) {
        adMapper.insert(ad);
        saveAdToRedisByPosition(ad.getPosition());
    }

    /**
     * 修改
     * @param ad
     */
    @Override
    public void update(Ad ad) {
        //获取之前的广告位置
        String position = adMapper.selectByPrimaryKey(ad.getId()).getPosition();
        saveAdToRedisByPosition(position);
        adMapper.updateByPrimaryKeySelective(ad);
        if(!position.equals(ad.getPosition())){
            saveAdToRedisByPosition(ad.getPosition());
        }
    }

    /**
     *  删除
     * @param id
     */
    @Override
    public void delete(Integer id) {
        Ad ad = adMapper.selectByPrimaryKey(id);
        adMapper.deleteByPrimaryKey(id);
        saveAdToRedisByPosition(ad.getPosition());
    }

    private List<Ad> getAds(String position) {
        Example example=new Example(Ad.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("position",position);
        //开始时间小于等于当前时间
        criteria.andLessThanOrEqualTo("startTime", new Date());
        //截止时间大于等于当前时间
        criteria.andGreaterThanOrEqualTo("endTime", new Date());
        criteria.andEqualTo("status", "1");
        return adMapper.selectByExample(example);
    }

    /**
     * 根据位置查询广告列表
     * @description
     * @author huiwang45@iflytek.com
     * @date 2020/05/07 16:24
     * @param
     * @return
     */
    @Override
    public List<Ad> findByPosition(String position) {
        //List<Ad> adList = getAds(position);
        System.out.println("从AdService缓存中提取数据"+position);
        return (List<Ad>)redisTemplate.boundHashOps(CacheKey.AD).get(position);
    }



    /**
     * 将某个位置的广告存入缓存
     * @description
     * @author huiwang45@iflytek.com
     * @date 2020/05/19 15:08
     * @param
     * @return
     */
    @Override
    public void saveAdToRedisByPosition(String position){

        //根据位置查询广告列表
        List<Ad> Adlist = getAds(position);
        redisTemplate.boundHashOps(CacheKey.AD).put(position, Adlist);
    }

    /**
     * 返回所有的广告位置
     * @description
     * @author huiwang45@iflytek.com
     * @date 2020/05/19 15:19
     * @param
     * @return
     */
    private List<String> getPositionList(){
        List<String> positionList = new ArrayList<String>();
        //首页广告轮播图
        positionList.add("web_index_lb");
        return positionList;
    }

    /**
     * 将全部广告数据存入缓存
     * @description
     * @author huiwang45@iflytek.com
     * @date 2020/05/19 15:08
     * @param
     * @return
     */
    @Override
    public void saveAllAdToRedis (){
        //循环所有的广告位置
        getPositionList().forEach(position -> {
            saveAdToRedisByPosition(position);
        });
    }

    /**
     * 构建查询条件
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap){
        Example example=new Example(Ad.class);
        Example.Criteria criteria = example.createCriteria();
        if(searchMap!=null){
            // 广告名称
            if(searchMap.get("name")!=null && !"".equals(searchMap.get("name"))){
                criteria.andLike("name","%"+searchMap.get("name")+"%");
            }
            // 广告位置
            if(searchMap.get("position")!=null && !"".equals(searchMap.get("position"))){
                criteria.andLike("position","%"+searchMap.get("position")+"%");
            }
            // 状态
            if(searchMap.get("status")!=null && !"".equals(searchMap.get("status"))){
                criteria.andLike("status","%"+searchMap.get("status")+"%");
            }
            // 图片地址
            if(searchMap.get("image")!=null && !"".equals(searchMap.get("image"))){
                criteria.andLike("image","%"+searchMap.get("image")+"%");
            }
            // URL
            if(searchMap.get("url")!=null && !"".equals(searchMap.get("url"))){
                criteria.andLike("url","%"+searchMap.get("url")+"%");
            }
            // 备注
            if(searchMap.get("remarks")!=null && !"".equals(searchMap.get("remarks"))){
                criteria.andLike("remarks","%"+searchMap.get("remarks")+"%");
            }

            // ID
            if(searchMap.get("id")!=null ){
                criteria.andEqualTo("id",searchMap.get("id"));
            }

        }
        return example;
    }

}
