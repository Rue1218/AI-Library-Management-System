package com.library.library.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user")
public class User extends BaseEntity {
    @TableField("username")
    private String username;

    @TableField("password")
    private String password;

    @TableField("real_name")
    private String realName;

    @TableField("phone")
    private String phone;

    @TableField("email")
    private String email;

    @TableField("role")
    private Integer role;

    @TableField("status")
    private Integer status;

    @TableField("avatar")
    private String avatar;

    @TableField("last_login_time")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate lastLoginTime;
}