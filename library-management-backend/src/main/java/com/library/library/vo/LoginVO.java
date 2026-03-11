package com.library.library.vo;

import lombok.Data;

@Data
public class LoginVO {
    private String token;
    private Long userId;
    private UserInfoVO user;
}