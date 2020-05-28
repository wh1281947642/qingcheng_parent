package com.qingcheng.service.impl;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;

/**
 * <p>
 * <code>RestClientFactory</code>
 * </p>
 * RestHighLevelClient 客户端
 * @author huiwang45@iflytek.com
 * @description
 * @date 2020/05/28 16:15
 */
public class RestClientFactory {

    public static RestHighLevelClient getRestHighLevelClient(String hostname,int port){
        HttpHost http=new HttpHost(hostname,port,"http");
        //rest构建器
        RestClientBuilder builder= RestClient.builder(http);
        //高级客户端对象 （连接）
        return new RestHighLevelClient(builder);
    }
}
