package com.library.library.controller;

import com.library.library.config.RequireRole;
import com.library.library.config.UserContextHolder;
import com.library.library.entity.User;
import com.library.library.service.UserService;
import com.library.library.utils.MD5Utils;
import com.library.library.vo.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @GetMapping
    @RequireRole("0")
    public Result<List<User>> listUsers() {
        try {
            List<User> users = userService.list();
            users.forEach(u -> u.setPassword(null));
            return Result.success(users);
        } catch (Exception e) {
            logger.error("获取用户列表失败", e);
            return Result.error("获取用户列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/profile")
    public Result<User> getProfile() {
        try {
            Long userId = UserContextHolder.getUserId();
            User user = userService.getById(userId);
            if (user != null) {
                user.setPassword(null);
            }
            return Result.success(user);
        } catch (Exception e) {
            logger.error("获取个人信息失败", e);
            return Result.error("获取个人信息失败: " + e.getMessage());
        }
    }

    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody User user) {
        try {
            Long userId = UserContextHolder.getUserId();
            user.setId(userId);
            userService.updateById(user);
            return Result.success(null);
        } catch (Exception e) {
            logger.error("更新个人信息失败", e);
            return Result.error("更新个人信息失败: " + e.getMessage());
        }
    }

    @PutMapping("/password")
    public Result<Void> updatePassword(@RequestBody java.util.Map<String, String> params) {
        try {
            Long userId = UserContextHolder.getUserId();
            String password = params.get("password");

            User user = new User();
            user.setId(userId);
            user.setPassword(MD5Utils.encrypt(password));
            userService.updateById(user);
            return Result.success(null);
        } catch (Exception e) {
            logger.error("更新密码失败", e);
            return Result.error("更新密码失败: " + e.getMessage());
        }
    }

    @PostMapping
    @RequireRole("0")
    public Result<Void> createUser(@RequestBody User user) {
        user.setRole(1);
        user.setStatus(1);
        userService.save(user);
        return Result.success(null);
    }

    @PutMapping("/{id}")
    @RequireRole("0")
    public Result<Void> updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        userService.updateById(user);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    @RequireRole("0")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.removeById(id);
        return Result.success(null);
    }

    @PutMapping("/{id}/status")
    @RequireRole("0")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        User user = new User();
        user.setId(id);
        user.setStatus(status);
        userService.updateById(user);
        return Result.success(null);
    }
}
