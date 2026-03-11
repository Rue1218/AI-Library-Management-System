package com.library.library.controller;

import com.library.library.dto.LoginDTO;
import com.library.library.dto.RegisterDTO;
import com.library.library.service.UserService;
import com.library.library.vo.LoginVO;
import com.library.library.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        LoginVO result = userService.login(loginDTO);
        return Result.success(result);
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String token) {
        // TODO: 可加入黑名单机制
        return Result.success(null);
    }

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO registerDTO) {
        userService.register(registerDTO);
        return Result.success(null);
    }
}