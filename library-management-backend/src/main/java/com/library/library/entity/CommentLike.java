package com.library.library.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("comment_like")
public class CommentLike extends BaseEntity {
    @TableField("comment_id")
    private Long commentId;

    @TableField("user_id")
    private Long userId;
}
