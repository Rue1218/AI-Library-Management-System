package com.library.library.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.library.library.dto.LoginDTO;
import com.library.library.dto.RegisterDTO;
import com.library.library.entity.User;
import com.library.library.vo.LoginVO;

public interface UserService extends IService<User> {
    LoginVO login(LoginDTO loginDTO);
    
    /**
     * 用户注册
     */
    void register(RegisterDTO registerDTO);
    
    /**
     * 根据用户名查询用户
     */
    User findByUsername(String username);
    
    /**
     * 更新最后登录时间
     */
    void updateLastLoginTime(Long userId);
    
    /**
     * 统计用户数量
     */
    long countUsers();
}