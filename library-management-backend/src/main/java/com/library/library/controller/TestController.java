package com.library.library.controller;

import com.library.library.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
public class TestController {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/encode/{password}")
    public String encode(@PathVariable String password) {
        return encoder.encode(password);
    }

    @GetMapping("/updatePassword/{username}/{password}")
    public String updatePassword(@PathVariable String username, @PathVariable String password) {
        com.library.library.entity.User user = userMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.library.library.entity.User>()
                .eq("username", username)
        );
        if (user != null) {
            user.setPassword(encoder.encode(password));
            userMapper.updateById(user);
            return "密码已更新";
        }
        return "用户不存在";
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello, Library Management System!";
    }
}