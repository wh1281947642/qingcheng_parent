package com.qingcheng.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.service.goods.SkuSearchService;
import com.qingcheng.util.WebUtil;
import org.aspectj.weaver.ast.Var;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
        return "search";
    }
}
