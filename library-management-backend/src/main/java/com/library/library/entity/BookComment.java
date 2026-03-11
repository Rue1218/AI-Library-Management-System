package com.library.library.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("book_comment")
public class BookComment extends BaseEntity {
    @TableField("book_id")
    private Long bookId;

    @TableField("user_id")
    private Long userId;

    @TableField("rating")
    private Integer rating;

    @TableField("content")
    private String content;

    @TableField("like_count")
    private Integer likeCount;

    @TableField("reply_count")
    private Integer replyCount;

    @TableField("parent_id")
    private Long parentId;
}
