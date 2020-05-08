package com.qingcheng.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.pojo.system.Admin;
import com.qingcheng.service.system.AdminService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * <code>UserDetailsService</code>
 * </p>
 * @author huiwang45@iflytek.com
 * @description
 * @date 2020/04/23 14:51
 */
public class UserDetailsServiceImpl implements UserDetailsService {

    @Reference
    private AdminService adminService;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        System.out.println("经过UserDetailsServiceImpl");

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("loginName", s);
        map.put("status", "1");
        List<Admin> list = adminService.findList(map);
        if (CollectionUtils.isEmpty(list)){
            return null;
        }

        //实际项目中应该从数据库中提取用户的角色列表
        List<GrantedAuthority> grantedAuths = new ArrayList<GrantedAuthority>();
        grantedAuths.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        return new User(s, list.get(0).getPassword(), grantedAuths);
    }
}
