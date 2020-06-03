package com.qingcheng.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.service.goods.SkuSearchService;
import com.qingcheng.util.WebUtil;
import org.aspectj.weaver.ast.Var;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


import java.util.Map;

/**
 * <p>
 * <code>SearchController</code>
 * </p>
 * 
 * @author huiwang45@iflytek.com
 * @description
 * @date 2020/05/28 16:37
 */
@Controller
public class SearchController {

    @Reference
    private SkuSearchService skuSearchService;

    @GetMapping("/search")
    public String search(Model model,@RequestParam Map<String, String> searchMap ) throws Exception {
        //字符集处理(解决中文乱码)
        searchMap = WebUtil.convertCharsetToUTF8(searchMap);

        //没有页码，默认为1
        if(searchMap.get("pageNo")==null){
            searchMap.put("pageNo","1");
        }

        //页面传递会后端两个参数 sort:排序字段 sortOrder 排序规格（升序/降序）
        //排序字段
        if(StringUtils.isEmpty(searchMap.get("sort"))){
            searchMap.put("sort","");
        }
        //排序规格
        if(StringUtils.isEmpty(searchMap.get("sortOrder"))){
            searchMap.put("sortOrder","DESC");
        }

        System.out.println(searchMap.get("keywords"));
        Map result = skuSearchService.search(searchMap);
        System.out.println(result);
        model.addAttribute("result",result);

        //url处理
        StringBuffer url =new StringBuffer("/search.do?");
        for (String key : searchMap.keySet()) {
            url.append("&"+key+"="+searchMap.get(key));
        }
        model.addAttribute("url",url);

        model.addAttribute("searchMap",searchMap);

        //页码
        int pageNo = Integer.parseInt(searchMap.get("pageNo"));
        model.addAttribute("pageNo",pageNo);

        //得到总页数
        Long totalPages =(Long) result.get("totalPages");
        //开始页码
        int startPage = 1;
        //截至代码
        int endPage = totalPages.intValue();

        if(totalPages>5){
            startPage=pageNo-2;
            if(startPage<1){
                startPage=1;
            }
            endPage=startPage+4;
        }
        model.addAttribute("startPage",startPage);
        model.addAttribute("endPage",endPage);

        return "search";
    }
}
