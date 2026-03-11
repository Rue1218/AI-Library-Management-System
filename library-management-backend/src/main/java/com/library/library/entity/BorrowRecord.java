package com.library.library.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("borrow_record")
public class BorrowRecord extends BaseEntity {
    @TableField("user_id")
    private Long userId;

    @TableField("book_id")
    private Long bookId;

    @TableField(exist = false)
    private String bookTitle;

    @TableField(exist = false)
    private String userName;

    @TableField("borrow_code")
    private String borrowCode;

    @TableField("borrow_date")
    private LocalDate borrowDate;

    @TableField("due_date")
    private LocalDate dueDate;

    @TableField("return_date")
    private LocalDate returnDate;

    @TableField("status")
    private Integer status;  // 0:借阅中, 1:已归还, 2:逾期, 3:待审核(续借)

    @TableField("renew_count")
    private Integer renewCount;

    @TableField("overdue_fine")
    private Double overdueFine;

    @TableField("operator_id")
    private Long operatorId;
}