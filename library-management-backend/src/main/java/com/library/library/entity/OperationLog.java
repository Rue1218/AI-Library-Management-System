package com.library.library.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("operation_log")
public class OperationLog extends BaseEntity {
    @TableField("operator_id")
    private Long operatorId;

    @TableField("operation_type")
    private String operationType;

    @TableField("target_id")
    private Long targetId;

    @TableField("content")
    private String content;

    @TableField("ip_address")
    private String ipAddress;
}