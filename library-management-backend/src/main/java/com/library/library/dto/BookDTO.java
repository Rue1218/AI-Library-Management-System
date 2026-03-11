package com.library.library.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class BookDTO {
    private String isbn;
    private String title;
    private String author;
    private String publisher;
    private LocalDate publishDate;
    private String category;
    private String coverUrl;
    private String description;
    private Integer totalStock;
    private Integer availableStock;
    private String location;
}