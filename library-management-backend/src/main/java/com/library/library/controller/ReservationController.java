package com.library.library.controller;

import com.library.library.config.UserContextHolder;
import com.library.library.entity.Reservation;
import com.library.library.service.ReservationService;
import com.library.library.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reservation")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @PostMapping("/reserve")
    public Result<Reservation> reserveBook(@RequestBody java.util.Map<String, Long> params) {
        Long bookId = params.get("bookId");
        Long userId = UserContextHolder.getUserId();
        Reservation reservation = reservationService.reserveBook(bookId, userId);
        return Result.success(reservation);
    }

    @PutMapping("/cancel/{reservationId}")
    public Result<Void> cancelReservation(@PathVariable Long reservationId) {
        reservationService.cancelReservation(reservationId);
        return Result.success(null);
    }

    @GetMapping("/list")
    public Result<List<Reservation>> getMyReservations() {
        Long userId = UserContextHolder.getUserId();
        List<Reservation> reservations = reservationService.listUserReservations(userId);
        return Result.success(reservations);
    }

    @GetMapping("/count")
    public Result<Integer> getReserveCount() {
        Long userId = UserContextHolder.getUserId();
        int count = reservationService.getUserReserveCount(userId);
        return Result.success(count);
    }

    @GetMapping("/admin/list")
    public Result<List<Reservation>> getAllReservations() {
        List<Reservation> reservations = reservationService.listAllReservations();
        return Result.success(reservations);
    }

    @PutMapping("/admin/complete/{reservationId}")
    public Result<Void> completeReservation(@PathVariable Long reservationId) {
        reservationService.completeReservation(reservationId);
        return Result.success(null);
    }
}
