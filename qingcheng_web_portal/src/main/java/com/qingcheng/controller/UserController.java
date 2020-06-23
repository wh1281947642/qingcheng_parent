package com.qingcheng.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.entity.Result;
import com.qingcheng.pojo.user.User;
import com.qingcheng.service.user.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Reference
    private UserService userService;

    /**
     * 发送短信验证码
     * @description
     * @author huiwang45@iflytek.com
     * @date 2020/06/22 17:06
     * @param
     * @return
     */
    @GetMapping("/sendSms")
    public Result sendSms(String phone){
        userService.sendSms(phone);
        return new Result();
    }

    /**
     *
     *
     * @description
     * @author huiwang45@iflytek.com
     * @date 2020/06/23 09:58
     * @param
     * @return
     */
    @PostMapping("/save")
    public Result save(@RequestBody User user , String smsCode ){
        //密码加密
        BCryptPasswordEncoder encoder=new BCryptPasswordEncoder();
        String newpassword = encoder.encode(user.getPassword());
        user.setPassword(newpassword);
        userService.add(user,smsCode);
        return new Result();
    }
}
