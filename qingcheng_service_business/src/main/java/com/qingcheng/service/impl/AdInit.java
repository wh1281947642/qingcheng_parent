package com.qingcheng.service.impl;

import com.qingcheng.service.business.AdService;
import com.qingcheng.service.goods.CategoryService;
import com.qingcheng.service.system.AdminService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * <p>
 * <code>Init</code>
 * </p>
 * @author huiwang45@iflytek.com
 * @description
 * @date 2020/05/18 17:41
 */

@Component
public class AdInit implements InitializingBean {

    @Autowired
    private AdService adService;

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("---AdService:缓存预热-----");
        adService.saveAllAdToRedis();

    }
}
