package com.library.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.library.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    /**
     * 根据用户名查询用户（用于登录）
     */
    User findByUsername(@Param("username") String username);
    
    /**
     * 更新用户最后登录时间
     */
    void updateLastLoginTime(@Param("userId") Long userId, @Param("loginTime") LocalDate loginTime);

    /**
     * 统计指定日期之后创建的用户数量
     */
    @Select("SELECT COUNT(*) FROM user WHERE DATE(create_time) >= DATE(#{startDate})")
    Long countUsersCreatedAfter(@Param("startDate") LocalDate startDate);

    /**
     * 统计指定日期范围内创建的用户数量
     */
    @Select("SELECT COUNT(*) FROM user WHERE DATE(create_time) BETWEEN DATE(#{startDate}) AND DATE(#{endDate})")
    Long countUsersCreatedBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}