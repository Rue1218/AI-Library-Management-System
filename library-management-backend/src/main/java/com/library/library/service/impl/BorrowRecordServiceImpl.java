package com.library.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.library.entity.Book;
import com.library.library.entity.BorrowRecord;
import com.library.library.entity.User;
import com.library.library.exception.UnauthorizedException;
import com.library.library.mapper.BookMapper;
import com.library.library.mapper.BorrowRecordMapper;
import com.library.library.service.BookService;
import com.library.library.service.BorrowRecordService;
import com.library.library.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class BorrowRecordServiceImpl extends ServiceImpl<BorrowRecordMapper, BorrowRecord> implements BorrowRecordService {

    @Autowired
    private BookService bookService;

    @Autowired
    private UserService userService;

    @Override
    @Transactional
    public BorrowRecord borrowBook(Long bookId, Long operatorId) {
        Book book = bookService.getById(bookId);
        if (book == null) {
            throw new UnauthorizedException("图书不存在");
        }
        if (book.getAvailableStock() <= 0) {
            throw new UnauthorizedException("图书库存不足，无法借阅");
        }

        // 减少可借库存
        book.setAvailableStock(book.getAvailableStock() - 1);
        bookService.updateById(book);

        // 创建借阅记录
        BorrowRecord record = new BorrowRecord();
        record.setBookId(bookId);
        record.setUserId(operatorId); // 或使用当前登录用户（如果是用户自助）
        record.setBorrowCode(generateBorrowCode());
        record.setBorrowDate(LocalDate.now());
        record.setDueDate(LocalDate.now().plusDays(30)); // 30天应还
        record.setStatus(0); // 借阅中
        record.setRenewCount(0);
        record.setOverdueFine(0.0);
        record.setOperatorId(operatorId);
        save(record);

        return record;
    }

    @Override
    @Transactional
    public void returnBook(Long recordId, Long operatorId) {
        BorrowRecord record = getById(recordId);
        if (record == null) {
            throw new UnauthorizedException("借阅记录不存在");
        }
        if (record.getStatus() == 1) {
            throw new UnauthorizedException("该图书已归还");
        }

        // 增加图书可借库存
        Book book = bookService.getById(record.getBookId());
        if (book != null) {
            book.setAvailableStock(book.getAvailableStock() + 1);
            bookService.updateById(book);
        }

        // 计算逾期费用
        LocalDate today = LocalDate.now();
        if (record.getDueDate().isBefore(today)) {
            long overdueDays = java.time.temporal.ChronoUnit.DAYS.between(record.getDueDate(), today);
            if (overdueDays > 0) {
                double overdueFine = overdueDays * 0.5; // 每天0.5元
                record.setOverdueFine(overdueFine);
                record.setStatus(2); // 标记为逾期
            } else {
                record.setStatus(1);
            }
        } else {
            record.setStatus(1);
        }

        // 更新还书日期和状态
        record.setReturnDate(LocalDate.now());
        updateById(record);
    }

    @Override
    public List<BorrowRecord> listRecords(Long userId, String role) {
        LambdaQueryWrapper<BorrowRecord> queryWrapper = new LambdaQueryWrapper<>();
        // 管理员查看所有，普通用户仅查看自己的
        if ("1".equals(role)) {
            queryWrapper.eq(BorrowRecord::getUserId, userId);
        }
        queryWrapper.orderByAsc(BorrowRecord::getBorrowCode);
        List<BorrowRecord> records = list(queryWrapper);
        
        // 填充图书标题和用户名
        for (BorrowRecord record : records) {
            Book book = bookService.getById(record.getBookId());
            if (book != null) {
                record.setBookTitle(book.getTitle());
            }
            User user = userService.getById(record.getUserId());
            if (user != null) {
                record.setUserName(user.getUsername());
            }
        }
        return records;
    }

    @Override
    public List<BorrowRecord> listOverdueRecords(LocalDate today) {
        LambdaQueryWrapper<BorrowRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BorrowRecord::getStatus, 0)
                .lt(BorrowRecord::getDueDate, today);
        return list(queryWrapper);
    }

    @Override
    @Transactional
    public void renewBook(Long recordId) {
        BorrowRecord record = getById(recordId);
        if (record == null) {
            throw new UnauthorizedException("借阅记录不存在");
        }
        if (record.getStatus() != 0) {
            throw new UnauthorizedException("只有借阅中的图书才能续借");
        }
        if (record.getRenewCount() >= 2) {
            throw new UnauthorizedException("续借次数已达上限（2次）");
        }
        // 续借30天
        record.setDueDate(record.getDueDate().plusDays(30));
        record.setRenewCount(record.getRenewCount() + 1);
        updateById(record);
    }

    private String generateBorrowCode() {
        return "BR" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    @Override
    public long countBorrows() {
        return count();
    }

    @Override
    public long countOverdue(LocalDate today) {
        LambdaQueryWrapper<BorrowRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BorrowRecord::getStatus, 0)
                .lt(BorrowRecord::getDueDate, today);
        return count(queryWrapper);
    }

    @Override
    public List<Map<String, Object>> getBorrowTrend(int days) {
        LocalDate today = LocalDate.now();
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LambdaQueryWrapper<BorrowRecord> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(BorrowRecord::getBorrowDate, date);
            long count = count(queryWrapper);
            
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("date", date.toString());
            map.put("count", count);
            result.add(map);
        }
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getHotBooks(int limit) {
        return baseMapper.getHotBooks(limit);
    }

    @Override
    public void deleteRecord(Long recordId) {
        BorrowRecord record = getById(recordId);
        if (record == null) {
            throw new UnauthorizedException("借阅记录不存在");
        }
        if (record.getStatus() == 0) {
            throw new UnauthorizedException("借阅中的记录无法删除");
        }
        removeById(recordId);
    }
}