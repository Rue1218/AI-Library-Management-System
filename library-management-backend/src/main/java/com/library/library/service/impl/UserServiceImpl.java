package com.library.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.library.dto.LoginDTO;
import com.library.library.dto.RegisterDTO;
import com.library.library.entity.User;
import com.library.library.exception.UnauthorizedException;
import com.library.library.exception.BusinessException;
import com.library.library.mapper.UserMapper;
import com.library.library.service.UserService;
import com.library.library.utils.JwtTokenProvider;
import com.library.library.utils.MD5Utils;
import com.library.library.vo.LoginVO;
import com.library.library.vo.UserInfoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private static final String DB_SALT = "library";

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, loginDTO.getUsername());
        User user = getOne(queryWrapper);

        if (user == null) {
            throw new UnauthorizedException("用户不存在");
        }

        if (!user.getStatus().equals(1)) {
            throw new UnauthorizedException("账户已被禁用，请联系管理员");
        }

        // MD5加密验证
        String inputPassword = loginDTO.getPassword();
        String storedPassword = user.getPassword();
        
        if (!MD5Utils.verify(inputPassword, storedPassword)) {
            throw new UnauthorizedException("密码错误");
        }

        // 角色校验
        if (!user.getRole().toString().equals(loginDTO.getRole())) {
            throw new UnauthorizedException("角色选择错误，请选择正确的角色登录");
        }

        // 更新最后登录时间
        updateLastLoginTime(user.getId());

        // 生成 JWT Token
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole());
        
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUserId(user.getId());
        
        // 组装用户信息
        UserInfoVO userInfo = new UserInfoVO();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setRealName(user.getRealName());
        userInfo.setPhone(user.getPhone());
        userInfo.setEmail(user.getEmail());
        userInfo.setRole(user.getRole());
        userInfo.setStatus(user.getStatus());
        userInfo.setAvatar(user.getAvatar());
        loginVO.setUser(userInfo);

        return loginVO;
    }

    @Override
    public User findByUsername(String username) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        return getOne(queryWrapper);
    }

    @Override
    public void updateLastLoginTime(Long userId) {
        User user = new User();
        user.setId(userId);
        user.setLastLoginTime(LocalDate.now());
        updateById(user);
    }

    @Override
    public void register(RegisterDTO registerDTO) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, registerDTO.getUsername());
        User existingUser = getOne(queryWrapper);
        if (existingUser != null) {
            throw new BusinessException("用户名已存在");
        }

        // 检查密码和确认密码是否一致
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(MD5Utils.encrypt(registerDTO.getPassword()));
        user.setRealName(registerDTO.getRealName());
        user.setPhone(registerDTO.getPhone());
        user.setEmail(registerDTO.getEmail());
        user.setRole(Integer.parseInt(registerDTO.getRole()));
        user.setStatus(1);

        save(user);
    }

    @Override
    public long countUsers() {
        return count();
    }
}