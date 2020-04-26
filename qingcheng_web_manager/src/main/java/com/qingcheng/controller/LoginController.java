package com.qingcheng.controller;

import org.omg.IOP.ServiceContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * <code>LoginController</code>
 * </p>
 *
 * @author huiwang45@iflytek.com
 * @description
 * @date 2020/04/24 17:54
 */

@RestController
@RequestMapping("/login")
public class LoginController {

    @GetMapping("/name")
    public Map showName(){

        //认证对象
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        //登录账号
        String name = authentication.getName();
        HashMap<Object, Object> map = new HashMap<>();
        map.put("name", name);
        return map;
    }
}
