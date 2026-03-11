package com.library.library.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.library.library.entity.BorrowRecord;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface BorrowRecordService extends IService<BorrowRecord> {
    /**
     * 借书
     */
    BorrowRecord borrowBook(Long bookId, Long operatorId);

    /**
     * 还书
     */
    void returnBook(Long recordId, Long operatorId);

    /**
     * 查询借阅记录（根据角色过滤）
     */
    List<BorrowRecord> listRecords(Long userId, String role);

    /**
     * 查询逾期记录
     */
    List<BorrowRecord> listOverdueRecords(LocalDate today);

    /**
     * 续借
     */
    void renewBook(Long recordId);
    
    /**
     * 统计借阅总数
     */
    long countBorrows();
    
    /**
     * 统计逾期数量
     */
    long countOverdue(LocalDate today);
    
    /**
     * 统计近N天的借阅数据
     */
    List<Map<String, Object>> getBorrowTrend(int days);
    
    /**
     * 获取热门图书
     */
    List<Map<String, Object>> getHotBooks(int limit);

    /**
     * 删除借阅记录
     */
    void deleteRecord(Long recordId);
}