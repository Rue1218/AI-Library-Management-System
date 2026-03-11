package com.library.library.controller;

import com.library.library.entity.BorrowRecord;
import com.library.library.service.BorrowRecordService;
import com.library.library.config.UserContextHolder;
import com.library.library.vo.Result;
import com.library.library.mapper.BookMapper;
import com.library.library.mapper.UserMapper;
import com.library.library.mapper.BorrowRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/borrow")
public class BorrowController {

    @Autowired
    private BorrowRecordService borrowRecordService;
    
    @Autowired
    private BookMapper bookMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private BorrowRecordMapper borrowRecordMapper;

    @PostMapping("/borrow")
    public Result<BorrowRecord> borrowBook(@RequestBody java.util.Map<String, Long> params) {
        Long bookId = params.get("bookId");
        Long operatorId = UserContextHolder.getUserId();
        BorrowRecord record = borrowRecordService.borrowBook(bookId, operatorId);
        return Result.success(record);
    }

    @PutMapping("/return/{recordId}")
    public Result<Void> returnBook(@PathVariable Long recordId) {
        Long operatorId = UserContextHolder.getUserId();
        borrowRecordService.returnBook(recordId, operatorId);
        return Result.success(null);
    }

    @GetMapping("/records")
    public Result<List<BorrowRecord>> getRecords() {
        Long userId = UserContextHolder.getUserId();
        String role = UserContextHolder.getRole() != null ? UserContextHolder.getRole().toString() : "1";
        List<BorrowRecord> records = borrowRecordService.listRecords(userId, role);
        return Result.success(records);
    }

    @GetMapping("/overdue")
    public Result<List<BorrowRecord>> getOverdueRecords() {
        List<BorrowRecord> records = borrowRecordService.listOverdueRecords(LocalDate.now());
        return Result.success(records);
    }

    @PutMapping("/renew/{recordId}")
    public Result<Void> renewBook(@PathVariable Long recordId) {
        borrowRecordService.renewBook(recordId);
        return Result.success(null);
    }

    @GetMapping("/stats")
    public Result<java.util.Map<String, Object>> getUserStats() {
        Long userId = UserContextHolder.getUserId();
        String role = UserContextHolder.getRole() != null ? UserContextHolder.getRole().toString() : "1";
        
        java.util.List<BorrowRecord> records = borrowRecordService.listRecords(userId, role);
        
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("borrowingCount", records.stream().filter(r -> r.getStatus() == 0).count());
        stats.put("returnedCount", records.stream().filter(r -> r.getStatus() == 1).count());
        stats.put("overdueCount", records.stream().filter(r -> r.getStatus() == 2 || (r.getStatus() == 0 && r.getDueDate() != null && r.getDueDate().isBefore(java.time.LocalDate.now()))).count());
        
        return Result.success(stats);
    }

    @GetMapping("/admin/stats")
    public Result<java.util.Map<String, Object>> getAdminStats() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        
        // 图书总数
        com.library.library.service.BookService bookService = 
            com.library.library.config.ApplicationContextHolder.getBean(com.library.library.service.BookService.class);
        stats.put("books", bookService.countBooks());
        
        // 用户总数
        com.library.library.service.UserService userService = 
            com.library.library.config.ApplicationContextHolder.getBean(com.library.library.service.UserService.class);
        stats.put("users", userService.count());
        
        // 借阅总次数
        stats.put("borrows", borrowRecordService.count());
        
        // 逾期数量
        java.util.List<BorrowRecord> overdueList = borrowRecordService.listOverdueRecords(java.time.LocalDate.now());
        stats.put("overdue", overdueList.size());
        
        // 增长率数据
        stats.put("booksGrowth", calculateBooksGrowth());
        stats.put("usersGrowth", calculateUsersGrowth());
        stats.put("borrowsGrowth", calculateBorrowsGrowth());
        stats.put("overdueGrowth", calculateOverdueGrowth());
        
        return Result.success(stats);
    }
    
    private Double calculateBooksGrowth() {
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate thisMonthStart = today.withDayOfMonth(1);
        java.time.LocalDate lastMonthStart = today.minusMonths(1).withDayOfMonth(1);
        java.time.LocalDate lastMonthEnd = today.minusDays(1).withDayOfMonth(1);
        
        Long thisMonthCount = bookMapper.countBooksCreatedAfter(thisMonthStart);
        Long lastMonthCount = bookMapper.countBooksCreatedBetween(lastMonthStart, lastMonthEnd);
        
        thisMonthCount = thisMonthCount != null ? thisMonthCount : 0L;
        lastMonthCount = lastMonthCount != null ? lastMonthCount : 0L;
        
        if (lastMonthCount == 0 || thisMonthCount == 0) {
            return 0.0;
        }
        return ((thisMonthCount - lastMonthCount) * 100.0) / lastMonthCount;
    }
    
    private Double calculateUsersGrowth() {
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate thisMonthStart = today.withDayOfMonth(1);
        java.time.LocalDate lastMonthStart = today.minusMonths(1).withDayOfMonth(1);
        java.time.LocalDate lastMonthEnd = today.minusDays(1).withDayOfMonth(1);
        
        Long thisMonthCount = userMapper.countUsersCreatedAfter(thisMonthStart);
        Long lastMonthCount = userMapper.countUsersCreatedBetween(lastMonthStart, lastMonthEnd);
        
        thisMonthCount = thisMonthCount != null ? thisMonthCount : 0L;
        lastMonthCount = lastMonthCount != null ? lastMonthCount : 0L;
        
        if (lastMonthCount == 0 || thisMonthCount == 0) {
            return 0.0;
        }
        return ((thisMonthCount - lastMonthCount) * 100.0) / lastMonthCount;
    }
    
    private Double calculateBorrowsGrowth() {
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate weekAgo = today.minusDays(7);
        java.time.LocalDate twoWeeksAgo = today.minusDays(14);
        
        Long thisWeekCount = borrowRecordMapper.countBorrowsBetween(weekAgo, today);
        Long lastWeekCount = borrowRecordMapper.countBorrowsBetween(twoWeeksAgo, weekAgo);
        
        thisWeekCount = thisWeekCount != null ? thisWeekCount : 0L;
        lastWeekCount = lastWeekCount != null ? lastWeekCount : 0L;
        
        if (lastWeekCount == 0 || thisWeekCount == 0) {
            return 0.0;
        }
        return ((thisWeekCount - lastWeekCount) * 100.0) / lastWeekCount;
    }
    
    private Double calculateOverdueGrowth() {
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate weekAgo = today.minusDays(7);
        java.time.LocalDate twoWeeksAgo = today.minusDays(14);
        
        Long thisWeekOverdue = borrowRecordMapper.countOverdueBetween(weekAgo, today);
        Long lastWeekOverdue = borrowRecordMapper.countOverdueBetween(twoWeeksAgo, weekAgo);
        
        thisWeekOverdue = thisWeekOverdue != null ? thisWeekOverdue : 0L;
        lastWeekOverdue = lastWeekOverdue != null ? lastWeekOverdue : 0L;
        
        if (lastWeekOverdue == 0 || thisWeekOverdue == 0) {
            return 0.0;
        }
        return ((thisWeekOverdue - lastWeekOverdue) * 100.0) / lastWeekOverdue;
    }

    @GetMapping("/trend")
    public Result<java.util.List<java.util.Map<String, Object>>> getBorrowTrend() {
        java.time.LocalDate today = java.time.LocalDate.now();
        java.util.List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
        
        for (int i = 6; i >= 0; i--) {
            java.time.LocalDate date = today.minusDays(i);
            java.time.LocalDateTime startOfDay = date.atStartOfDay();
            java.time.LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
            
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<BorrowRecord> wrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            wrapper.between(BorrowRecord::getBorrowDate, date, date);
            long count = borrowRecordService.count(wrapper);
            
            java.util.Map<String, Object> item = new java.util.HashMap<>();
            item.put("date", date.toString());
            item.put("count", count);
            result.add(item);
        }
        
        return Result.success(result);
    }

    @GetMapping("/hot-books")
    public Result<java.util.List<java.util.Map<String, Object>>> getHotBooks(
            @RequestParam(defaultValue = "10") Integer limit) {
        java.util.List<BorrowRecord> records = borrowRecordService.list();
        
        // 统计每本书的借阅次数
        java.util.Map<Long, Long> bookCount = new java.util.HashMap<>();
        for (BorrowRecord record : records) {
            if (record.getBookId() != null) {
                bookCount.merge(record.getBookId(), 1L, Long::sum);
            }
        }
        
        // 排序取前N个
        java.util.List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
        bookCount.entrySet().stream()
            .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
            .limit(limit)
            .forEach(entry -> {
                java.util.Map<String, Object> item = new java.util.HashMap<>();
                item.put("bookId", entry.getKey());
                item.put("count", entry.getValue());
                
                // 获取完整图书信息
                com.library.library.service.BookService bookService = 
                    com.library.library.config.ApplicationContextHolder.getBean(com.library.library.service.BookService.class);
                com.library.library.entity.Book book = bookService.getById(entry.getKey());
                if (book != null) {
                    item.put("id", book.getId());
                    item.put("title", book.getTitle());
                    item.put("author", book.getAuthor());
                    item.put("publisher", book.getPublisher());
                    item.put("coverUrl", book.getCoverUrl());
                    item.put("availableStock", book.getAvailableStock());
                    item.put("totalStock", book.getTotalStock());
                    item.put("category", book.getCategoryId());
                } else {
                    item.put("title", "未知图书");
                }
                
                result.add(item);
            });
        
        return Result.success(result);
    }

    @DeleteMapping("/record/{recordId}")
    public Result<Void> deleteRecord(@PathVariable Long recordId) {
        borrowRecordService.deleteRecord(recordId);
        return Result.success(null);
    }
}