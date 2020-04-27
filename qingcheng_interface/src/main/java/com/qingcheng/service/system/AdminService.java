package com.qingcheng.service.system;
import com.qingcheng.entity.PageResult;
import com.qingcheng.entity.Result;
import com.qingcheng.pojo.system.Admin;

import java.util.*;

/**
 * admin业务逻辑层
 */
public interface AdminService {


    public List<Admin> findAll();


    public PageResult<Admin> findPage(int page, int size);


    public List<Admin> findList(Map<String, Object> searchMap);


    public PageResult<Admin> findPage(Map<String, Object> searchMap, int page, int size);


    public Admin findById(Integer id);

    public void add(Admin admin);


    public void update(Admin admin);


    public void delete(Integer id);

    /**
     * 修改用户密码
     * @description
     * @author huiwang45@iflytek.com
     * @date 2020/04/27 14:07
     * @param
     * @return
     */
    Result updatePassword(String loginName, String oldPassword, String newPassword);
}
