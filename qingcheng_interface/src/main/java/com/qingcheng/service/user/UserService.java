package com.qingcheng.service.user;
import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.user.User;

import java.util.*;

/**
 * user业务逻辑层
 */
public interface UserService {


    public List<User> findAll();


    public PageResult<User> findPage(int page, int size);


    public List<User> findList(Map<String, Object> searchMap);


    public PageResult<User> findPage(Map<String, Object> searchMap, int page, int size);


    public User findById(String username);

    public void add(User user);


    public void update(User user);


    public void delete(String username);

    /**
     * 发送短信验证码
     * @description
     * @author huiwang45@iflytek.com
     * @date 2020/06/22 16:19
     * @param
     * @return
     */
    public void sendSms(String phone);

    /**
     * 增加
     * @description
     * @author huiwang45@iflytek.com
     * @date 2020/06/23 09:24
     * @param user 用户
     * @param smsCode 验证码
     * @return
     */
    public void add(User user,String smsCode);

}
