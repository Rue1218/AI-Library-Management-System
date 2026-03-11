package com.library.library.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("reservation")
public class Reservation extends BaseEntity {
    @TableField("user_id")
    private Long userId;

    @TableField("book_id")
    private Long bookId;

    @TableField(exist = false)
    private String bookTitle;

    @TableField(exist = false)
    private String userName;

    @TableField("reserve_date")
    private LocalDate reserveDate;

    @TableField("expire_date")
    private LocalDate expireDate;

    @TableField("status")
    private Integer status;  // 0:等待中, 1:可取书, 2:已取消, 3:已完成

    @TableField("notified")
    private Boolean notified = false;
}