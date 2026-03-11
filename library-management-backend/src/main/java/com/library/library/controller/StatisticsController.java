package com.library.library.controller;

import com.library.library.entity.BorrowRecord;
import com.library.library.mapper.BorrowRecordMapper;
import com.library.library.mapper.UserMapper;
import com.library.library.mapper.BookMapper;
import com.library.library.service.BookService;
import com.library.library.service.BorrowRecordService;
import com.library.library.service.UserService;
import com.library.library.vo.Result;
import com.library.library.vo.StatisticsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    @Autowired
    private BookService bookService;

    @Autowired
    private UserService userService;

    @Autowired
    private BorrowRecordService borrowRecordService;

    @Autowired
    private BorrowRecordMapper borrowRecordMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BookMapper bookMapper;

    @GetMapping
    public Result<StatisticsVO> getStatistics() {
        StatisticsVO statistics = new StatisticsVO();
        
        statistics.setTotalBooks(bookService.countBooks());
        statistics.setTotalUsers(userService.countUsers());
        statistics.setTotalBorrows(borrowRecordService.countBorrows());
        statistics.setOverdueCount(borrowRecordService.countOverdue(LocalDate.now()));
        
        // 计算增长率
        statistics.setBooksGrowth(calculateBooksGrowth());
        statistics.setUsersGrowth(calculateUsersGrowth());
        statistics.setBorrowsGrowth(calculateBorrowsGrowth());
        statistics.setOverdueGrowth(calculateOverdueGrowth());
        
        List<Map<String, Object>> trendData = borrowRecordService.getBorrowTrend(7);
        List<StatisticsVO.TrendData> trendList = trendData.stream()
                .map(m -> {
                    StatisticsVO.TrendData td = new StatisticsVO.TrendData();
                    td.setDate((String) m.get("date"));
                    td.setCount(((Number) m.get("count")).longValue());
                    return td;
                })
                .toList();
        statistics.setBorrowTrend(trendList);
        
        List<Map<String, Object>> hotBooksData = borrowRecordService.getHotBooks(5);
        List<StatisticsVO.HotBook> hotBooks = hotBooksData.stream()
                .map(m -> {
                    StatisticsVO.HotBook hb = new StatisticsVO.HotBook();
                    hb.setTitle((String) m.get("title"));
                    hb.setCount(((Number) m.get("count")).longValue());
                    return hb;
                })
                .toList();
        statistics.setHotBooks(hotBooks);
        
        List<Map<String, Object>> categoryData = bookService.getCategoryDistribution();
        List<StatisticsVO.CategoryData> categoryList = categoryData.stream()
                .map(m -> {
                    StatisticsVO.CategoryData cd = new StatisticsVO.CategoryData();
                    cd.setName((String) m.get("name"));
                    cd.setValue(((Number) m.get("value")).longValue());
                    return cd;
                })
                .toList();
        statistics.setCategoryDistribution(categoryList);
        
        return Result.success(statistics);
    }

    private Double calculateBooksGrowth() {
        LocalDate today = LocalDate.now();
        LocalDate thisMonthStart = today.withDayOfMonth(1);
        LocalDate lastMonthStart = today.minusMonths(1).withDayOfMonth(1);
        LocalDate lastMonthEnd = today.minusDays(1).withDayOfMonth(1);
        
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
        LocalDate today = LocalDate.now();
        LocalDate thisMonthStart = today.withDayOfMonth(1);
        LocalDate lastMonthStart = today.minusMonths(1).withDayOfMonth(1);
        LocalDate lastMonthEnd = today.minusDays(1).withDayOfMonth(1);
        
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
        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(7);
        LocalDate twoWeeksAgo = today.minusDays(14);
        
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
        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(7);
        LocalDate twoWeeksAgo = today.minusDays(14);
        
        Long thisWeekOverdue = borrowRecordMapper.countOverdueBetween(weekAgo, today);
        Long lastWeekOverdue = borrowRecordMapper.countOverdueBetween(twoWeeksAgo, weekAgo);
        
        thisWeekOverdue = thisWeekOverdue != null ? thisWeekOverdue : 0L;
        lastWeekOverdue = lastWeekOverdue != null ? lastWeekOverdue : 0L;
        
        if (lastWeekOverdue == 0 || thisWeekOverdue == 0) {
            return 0.0;
        }
        return ((thisWeekOverdue - lastWeekOverdue) * 100.0) / lastWeekOverdue;
    }
}
