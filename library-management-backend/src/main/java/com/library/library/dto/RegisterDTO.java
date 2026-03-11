package com.library.library.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class RegisterDTO {
    @NotBlank(message = "账号不能为空")
    @Size(min = 3, max = 20, message = "账号长度必须在3-20个字符之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String password;

    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    private String phone;

    private String email;

    @NotBlank(message = "角色不能为空")
    private String role;
}
