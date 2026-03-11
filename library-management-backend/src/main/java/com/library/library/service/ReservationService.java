package com.library.library.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.library.library.entity.Reservation;

import java.util.List;

public interface ReservationService extends IService<Reservation> {
    /**
     * 预约图书
     */
    Reservation reserveBook(Long bookId, Long userId);

    /**
     * 取消预约
     */
    void cancelReservation(Long reservationId);

    /**
     * 获取用户预约列表
     */
    List<Reservation> listUserReservations(Long userId);

    /**
     * 获取用户预约统计
     */
    int getUserReserveCount(Long userId);

    /**
     * 获取所有预约列表（管理员）
     */
    List<Reservation> listAllReservations();

    /**
     * 完成预约（管理员确认取书）
     */
    void completeReservation(Long reservationId);
}
