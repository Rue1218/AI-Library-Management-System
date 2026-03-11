package com.library.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.library.entity.BorrowRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface BorrowRecordMapper extends BaseMapper<BorrowRecord> {
    /**
     * 查询用户当前借阅中的图书
     */
    List<BorrowRecord> selectActiveBorrows(@Param("userId") Long userId);
    
    /**
     * 查询逾期记录
     */
    List<BorrowRecord> selectOverdueRecords(@Param("today") LocalDate today);
    
    /**
     * 获取热门图书排行
     */
    @Select("SELECT b.title, COUNT(br.id) as count FROM borrow_record br " +
            "LEFT JOIN book b ON br.book_id = b.id " +
            "GROUP BY br.book_id ORDER BY count DESC LIMIT #{limit}")
    List<Map<String, Object>> getHotBooks(@Param("limit") int limit);

    /**
     * 统计指定日期范围内的借阅记录数量
     */
    @Select("SELECT COUNT(*) FROM borrow_record WHERE DATE(borrow_date) BETWEEN DATE(#{startDate}) AND DATE(#{endDate})")
    Long countBorrowsBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 统计指定日期范围内的逾期记录数量
     */
    @Select("SELECT COUNT(*) FROM borrow_record WHERE DATE(due_date) BETWEEN DATE(#{startDate}) AND DATE(#{endDate}) AND status = 0")
    Long countOverdueBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}