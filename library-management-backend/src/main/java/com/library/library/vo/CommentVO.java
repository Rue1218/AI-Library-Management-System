package com.library.library.vo;

import lombok.Data;

@Data
public class CommentVO {
    private Long id;
    private Long bookId;
    private Long userId;
    private String username;
    private String userAvatar;
    private Integer rating;
    private String content;
    private Integer likeCount;
    private Integer replyCount;
    private Long parentId;
    private Boolean isLiked;
    private String createTime;
}
