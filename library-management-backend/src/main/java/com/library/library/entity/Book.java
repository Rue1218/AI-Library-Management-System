package com.library.library.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("book")
public class Book extends BaseEntity {
    @TableField("isbn")
    private String isbn;

    @TableField("title")
    private String title;

    @TableField("author")
    private String author;

    @TableField("publisher")
    private String publisher;

    @TableField("publish_date")
    private LocalDate publishDate;

    @TableField("category_id")
    private Long categoryId;

    @TableField(exist = false)
    private String category;

    @TableField("cover_url")
    private String coverUrl;

    @TableField("description")
    private String description;

    @TableField("total_stock")
    private Integer totalStock;

    @TableField("available_stock")
    private Integer availableStock;

    @TableField("location")
    private String location;
}