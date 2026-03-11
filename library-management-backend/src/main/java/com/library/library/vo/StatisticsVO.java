package com.library.library.vo;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class StatisticsVO {
    private Long totalBooks;
    private Long totalUsers;
    private Long totalBorrows;
    private Long overdueCount;
    
    private Double booksGrowth;
    private Double usersGrowth;
    private Double borrowsGrowth;
    private Double overdueGrowth;
    
    private List<TrendData> borrowTrend;
    private List<HotBook> hotBooks;
    private List<CategoryData> categoryDistribution;
    
    @Data
    public static class TrendData {
        private String date;
        private Long count;
    }
    
    @Data
    public static class HotBook {
        private String title;
        private Long count;
    }
    
    @Data
    public static class CategoryData {
        private String name;
        private Long value;
    }
}
