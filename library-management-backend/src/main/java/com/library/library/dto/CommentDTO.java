package com.library.library.dto;

import lombok.Data;

@Data
public class CommentDTO {
    private Long bookId;
    private Integer rating;
    private String content;
    private Long parentId;
}
