package com.library.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.library.entity.Book;
import com.library.library.entity.Reservation;
import com.library.library.entity.User;
import com.library.library.exception.UnauthorizedException;
import com.library.library.mapper.ReservationMapper;
import com.library.library.service.BookService;
import com.library.library.service.ReservationService;
import com.library.library.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReservationServiceImpl extends ServiceImpl<ReservationMapper, Reservation> implements ReservationService {

    @Autowired
    private BookService bookService;

    @Autowired
    private UserService userService;

    @Override
    @Transactional
    public Reservation reserveBook(Long bookId, Long userId) {
        Book book = bookService.getById(bookId);
        if (book == null) {
            throw new UnauthorizedException("图书不存在");
        }

        // 检查是否已预约
        LambdaQueryWrapper<Reservation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Reservation::getUserId, userId)
                .eq(Reservation::getBookId, bookId)
                .in(Reservation::getStatus, 0, 1); // 等待中或可取书
        Reservation existing = getOne(queryWrapper);
        if (existing != null) {
            throw new UnauthorizedException("您已预约过此图书");
        }

        Reservation reservation = new Reservation();
        reservation.setUserId(userId);
        reservation.setBookId(bookId);
        reservation.setReserveDate(LocalDate.now());
        reservation.setExpireDate(LocalDate.now().plusDays(7)); // 7天后过期
        reservation.setStatus(0); // 等待中
        save(reservation);

        return reservation;
    }

    @Override
    @Transactional
    public void cancelReservation(Long reservationId) {
        Reservation reservation = getById(reservationId);
        if (reservation == null) {
            throw new UnauthorizedException("预约记录不存在");
        }
        if (reservation.getStatus() == 2 || reservation.getStatus() == 3) {
            throw new UnauthorizedException("该预约已取消或已完成");
        }
        reservation.setStatus(2); // 已取消
        updateById(reservation);
    }

    @Override
    public List<Reservation> listUserReservations(Long userId) {
        LambdaQueryWrapper<Reservation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Reservation::getUserId, userId)
                .orderByDesc(Reservation::getReserveDate);
        List<Reservation> reservations = list(queryWrapper);
        
        // 填充图书标题
        for (Reservation reservation : reservations) {
            Book book = bookService.getById(reservation.getBookId());
            if (book != null) {
                reservation.setBookTitle(book.getTitle());
            }
        }
        return reservations;
    }

    @Override
    public int getUserReserveCount(Long userId) {
        LambdaQueryWrapper<Reservation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Reservation::getUserId, userId)
                .in(Reservation::getStatus, 0, 1); // 等待中或可取书
        return (int) count(queryWrapper);
    }

    @Override
    public List<Reservation> listAllReservations() {
        LambdaQueryWrapper<Reservation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Reservation::getId);
        List<Reservation> reservations = list(queryWrapper);
        
        // 填充图书标题和用户姓名
        for (Reservation reservation : reservations) {
            Book book = bookService.getById(reservation.getBookId());
            if (book != null) {
                reservation.setBookTitle(book.getTitle());
            }
            User user = userService.getById(reservation.getUserId());
            if (user != null) {
                reservation.setUserName(user.getUsername());
            }
        }
        return reservations;
    }

    @Override
    @Transactional
    public void completeReservation(Long reservationId) {
        Reservation reservation = getById(reservationId);
        if (reservation == null) {
            throw new UnauthorizedException("预约记录不存在");
        }
        reservation.setStatus(3); // 已完成
        updateById(reservation);
    }
}
