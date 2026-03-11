package com.library.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.library.entity.Book;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface BookMapper extends BaseMapper<Book> {
    /**
     * 批量更新图书库存（借书/还书时使用）
     */
    int updateStock(@Param("bookId") Long bookId, @Param("change") Integer change);
    
    /**
     * 根据分类统计图书数量
     */
    Long countByCategory(@Param("categoryId") Long categoryId);

    /**
     * 物理删除图书
     */
    @Update("DELETE FROM book WHERE id = #{id}")
    int physicallyDeleteById(@Param("id") Long id);

    /**
     * 统计指定日期之后创建的图书数量
     */
    @Select("SELECT COUNT(*) FROM book WHERE DATE(create_time) >= DATE(#{startDate})")
    Long countBooksCreatedAfter(@Param("startDate") LocalDate startDate);

    /**
     * 统计指定日期范围内创建的图书数量
     */
    @Select("SELECT COUNT(*) FROM book WHERE DATE(create_time) BETWEEN DATE(#{startDate}) AND DATE(#{endDate})")
    Long countBooksCreatedBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}