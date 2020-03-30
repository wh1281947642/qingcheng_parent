package com.qingcheng.controller.goods;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.entity.PageResult;
import com.qingcheng.entity.Result;
import com.qingcheng.pojo.goods.Goods;
import com.qingcheng.pojo.goods.Spu;
import com.qingcheng.service.goods.SpuService;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/spu")
public class SpuController {

    @Reference
    private SpuService spuService;

    @GetMapping("/findAll")
    public List<Spu> findAll(){
        return spuService.findAll();
    }

    @GetMapping("/findPage")
    public PageResult<Spu> findPage(int page, int size){
        return spuService.findPage(page, size);
    }

    @PostMapping("/findList")
    public List<Spu> findList(@RequestBody Map<String,Object> searchMap){
        return spuService.findList(searchMap);
    }

    @PostMapping("/findPage")
    public PageResult<Spu> findPage(@RequestBody Map<String,Object> searchMap,int page, int size){
        return  spuService.findPage(searchMap,page,size);
    }

    @GetMapping("/findById")
    public Spu findById(String id){
        return spuService.findById(id);
    }


    @PostMapping("/add")
    public Result add(@RequestBody Spu spu){
        spuService.add(spu);
        return new Result();
    }

    @PostMapping("/update")
    public Result update(@RequestBody Spu spu){
        spuService.update(spu);
        return new Result();
    }

    @GetMapping("/delete")
    public Result delete(String id){
        spuService.delete(id);
        return new Result();
    }

    /**
     * 保存商品
     * @description 
     * @author huiwang45@iflytek.com
     * @date 2020/03/30 15:33
     * @param
     * @return
     */
    @PostMapping("/saveGoods")
    public  Result save(@RequestBody Goods goods){
        this.spuService.saveGoods(goods);
        return new Result();
    }

    /**
     * 根据spuId查询Goods
     * @description
     * @author huiwang45@iflytek.com
     * @date 2020/03/30 15:33
     * @param id
     * @return Goods
     */
    @GetMapping("/findGoodsById")
    public Goods findGoodsById(String id){
        Goods goods = this.spuService.findGoodsById(id);
        return goods;
    }

    /**
     * 修改商品
     * @description
     * @author huiwang45@iflytek.com
     * @date 2020/03/30 15:33
     * @param goods
     * @return Result
     */
    @PostMapping("/updateGoods")
    public Result updateGoods(@RequestBody Goods goods){
        this.spuService.updateGoods(goods);
        return new Result();
    }
}
