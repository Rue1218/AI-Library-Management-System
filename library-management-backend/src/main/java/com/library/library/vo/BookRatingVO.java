package com.library.library.vo;

import lombok.Data;

@Data
public class BookRatingVO {
    private Double averageRating;
    private Integer totalComments;
    private Integer rating5;
    private Integer rating4;
    private Integer rating3;
    private Integer rating2;
    private Integer rating1;
}
