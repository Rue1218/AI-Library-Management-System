package com.library.library.vo;

import lombok.Data;

@Data
public class UserInfoVO {
    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private Integer role;
    private Integer status;
    private String avatar;
}