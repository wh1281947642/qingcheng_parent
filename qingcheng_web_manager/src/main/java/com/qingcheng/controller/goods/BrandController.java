package com.qingcheng.controller.goods;

import com.alibaba.dubbo.config.annotation.Reference;


import com.qingcheng.entity.PageResult;
import com.qingcheng.entity.Result;
import com.qingcheng.pojo.goods.Brand;
import com.qingcheng.service.goods.BrandService;
import org.springframework.web.bind.annotation.*;

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
@RestController
@RequestMapping("/brand")
public class BrandController {

    @Reference
    private BrandService brandService;

    @RequestMapping("/test")
    public String test(){
        return "test";
    }

    @RequestMapping("/test1")
    public String test1(){
        return brandService.test();
    }

    /**
     * 查询全部
     * @description
     * @author huiwang45@iflytek.com
     * @date 2020/03/12 15:21
     * @param
     * @return
     */
    @RequestMapping("/findAll")
    public List<Brand> findAll(){
        return brandService.findAll();
    }

    /**
     * 分页查询
     * @description
     * @author huiwang45@iflytek.com
     * @date 2020/03/12 15:21
     * @param
     * @return
     */
    @GetMapping("/findPage")
    public PageResult<Brand> findPage(int page,int size){
        return  brandService.findPage(page,size);
    }

    /**
     * example 条件查询
     * @description
     * @author huiwang45@iflytek.com
     * @date 2020/03/12 15:33
     * @param
     * @return
     */
    @PostMapping("/findList")
    public List<Brand> findList( @RequestBody Map searchMap){
        return brandService.findList(searchMap);
    }

    /**
     * 条件分页查询
     * @description
     * @author huiwang45@iflytek.com
     * @date 2020/03/12 15:55
     * @param
     * @return
     */
    @PostMapping("/findPage")
    public PageResult<Brand>  findPage(@RequestBody Map searchMap,int page,int size ){
        return brandService.findPage(searchMap,page,size);
    }

    /**
     * 根据ID查询品牌
     * @description
     * @author huiwang45@iflytek.com
     * @date 2020/03/12 16:05
     * @param
     * @return
     */
    @GetMapping("/findById")
    public Brand findById(Integer id){
        return brandService.findById(id);
    }

    /**
     * 品牌新增
     * @description
     * @author huiwang45@iflytek.com
     * @date 2020/03/12 16:09
     * @param
     * @return
     */
    @PostMapping("/add")
    public Result add(@RequestBody  Brand brand){
        brandService.add(brand);
        return new Result();
    }

    /**
     * 品牌修改
     * @description
     * @author huiwang45@iflytek.com
     * @date 2020/03/12 16:18
     * @param
     * @return
     */
    @PostMapping("/update")
    public Result update(@RequestBody  Brand brand){
        brandService.update(brand);
        return new Result();
    }

    /**
     * 品牌删除
     * @description
     * @author huiwang45@iflytek.com
     * @date 2020/03/12 16:24
     * @param
     * @return
     */
    @GetMapping("delete")
    public Result delete(Integer id){
        brandService.delete(id);
        return new Result();
    }
}
