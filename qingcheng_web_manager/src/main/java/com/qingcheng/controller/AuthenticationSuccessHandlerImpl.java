package com.qingcheng.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.pojo.system.LoginLog;
import com.qingcheng.service.system.LoginLogService;
import com.qingcheng.util.WebUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

/**
 * <p>
 * <code>AuthenticationSuccessHandlerImpl</code>
 * </p>
 * 登录成功处理器
 * @author huiwang45@iflytek.com
 * @description
 * @date 2020/04/26 15:20
 */
public class AuthenticationSuccessHandlerImpl implements AuthenticationSuccessHandler {

    @Reference
    private LoginLogService loginLogService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException, ServletException {

        //登录之后会调用
        System.out.println("登录成功了!!!");
        String loginName = authentication.getName();
        String ip = httpServletRequest.getRemoteAddr();
        String agent = httpServletRequest.getHeader("user-agent");
        LoginLog loginLog = new LoginLog();
        //当前登录用户
        loginLog.setLoginName(loginName);
        //当前登录时间
        loginLog.setLoginTime(new Date());
        //远程客户端的ip
        loginLog.setIp(ip);
        //地区
        loginLog.setLocation(WebUtil.getCityByIP(ip));
        //浏览器名称
        loginLog.setBrowserName(WebUtil.getBrowserName(agent));
        loginLogService.add(loginLog);

        httpServletRequest.getRequestDispatcher("main.html").forward(httpServletRequest,httpServletResponse );
    }
}
