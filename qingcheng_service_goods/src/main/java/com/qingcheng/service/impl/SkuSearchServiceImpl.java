package com.qingcheng.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.qingcheng.dao.BrandMapper;
import com.qingcheng.dao.SpecMapper;
import com.qingcheng.service.goods.SkuSearchService;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * <code>SkuSearchServiceImpl</code>
 * </p>
 * 
 * @author huiwang45@iflytek.com
 * @description
 * @date 2020/05/28 16:12
 */
@Service
public class SkuSearchServiceImpl implements SkuSearchService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private SpecMapper specMapper;


    @Override
    public Map search(Map<String,String> searchMap) {

        //1.封装请求对象
        SearchRequest searchRequest = new SearchRequest("sku");
        //设置查询类型
        searchRequest.types("doc");

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //布尔查询构建器
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        //1.1关键字搜索
        MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("name", searchMap.get("keywords"));
        boolQueryBuilder.must(matchQueryBuilder);

        //1.2商品分类的过滤
        if (!StringUtils.isEmpty(searchMap.get("category"))){
            TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("categoryName", searchMap.get("category"));
            boolQueryBuilder.filter(termQueryBuilder);
        }

        //1.3品牌的过滤
        if (!StringUtils.isEmpty(searchMap.get("brand"))){
            TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("brandName", searchMap.get("brand"));
            boolQueryBuilder.filter(termQueryBuilder);
        }

        //1.4规格过滤
        for (String key : searchMap.keySet()) {
            if (key.startsWith("spec.")){
                //如果是规格参数
                TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery(key+".keyword", searchMap.get(key));
                boolQueryBuilder.filter(termQueryBuilder);
            }
        }

        if (!StringUtils.isEmpty(searchMap.get("price"))){
            String[] prices = searchMap.get("price").split("-");
            //最低价格不等于0
            if (!"0".equals(prices[0])){
                RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("price").gte(prices[0]);
                boolQueryBuilder.filter(rangeQueryBuilder);
            }
            //如果价格有上限
            if (!"*".equals(prices[1])){
                RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("price").lte(prices[1]);
                boolQueryBuilder.filter(rangeQueryBuilder);
            }
        }

        //1.5价格筛选


        searchSourceBuilder.query(boolQueryBuilder);
        searchRequest.source(searchSourceBuilder);

        //商品分类（聚合查询）
        TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms("sku_category").field("categoryName");
        searchSourceBuilder.aggregation(termsAggregationBuilder);

        HashMap<String, Object> resultMap = new HashMap<>();
        try {
            //2.封装查询结果
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
            SearchHits searchHits = searchResponse.getHits();
            long totalHits = searchHits.getTotalHits();
            System.out.println("记录数:"+totalHits);
            SearchHit[] hits = searchHits.getHits();
            //2.1商品列表
            ArrayList<Map<String,Object>> resultList = new ArrayList<Map<String,Object>>();
            for (SearchHit hit : hits) {
                String source = hit.getSourceAsString();
                Map<String, Object> skuMap = hit.getSourceAsMap();
                resultList.add(skuMap);
            }
            resultMap.put("rows", resultList);

            //2.2商品分类列表
            Aggregations aggregations = searchResponse.getAggregations();
            Map<String, Aggregation> map = aggregations.getAsMap();
            Terms terms = (Terms)map.get("sku_category");
            List<? extends Terms.Bucket> buckets = terms.getBuckets();
            List<String> categoryList = buckets.stream().map(bucket -> bucket.getKeyAsString()).collect(Collectors.toList());
            resultMap.put("categoryList", categoryList);

            //分类名称
            String categoryName = "";
            //如果没有分类的信息,取分类列表第一个列表
            if (StringUtils.isEmpty(searchMap.get("category"))){
                if (!CollectionUtils.isEmpty(categoryList)){
                    categoryName =  categoryList.get(0);
                }
            }else {
                //取出参数中的分类
                categoryName = searchMap.get("category");
            }

            //2.3 品牌列表
            if (StringUtils.isEmpty(searchMap.get("brand"))){
                //查询品牌列表
                List<Map> brandList = this.brandMapper.findListByCategoryName(categoryName);
                resultMap.put("brandList", brandList);
            }

            //2.4规格列表
            List<Map> specList = this.specMapper.findListByCategoryName(categoryName);
            specList.forEach(spec->{
                //规格选项列表
                String[] options = ((String) spec.get("options")).split(",");
                spec.put("options", options);
            });
            resultMap.put("specList", specList);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return resultMap;
    }
}
