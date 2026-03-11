package com.library.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.library.entity.Reservation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.time.LocalDate;

@Mapper
public interface ReservationMapper extends BaseMapper<Reservation> {
    /**
     * 查找用户对某图书的有效预约
     */
    Reservation findActiveReservation(@Param("userId") Long userId, @Param("bookId") Long bookId);
    
    /**
     * 查询已到期的预约（可通知取书）
     */
    List<Reservation> selectExpiredReservations(@Param("today") LocalDate today);
}