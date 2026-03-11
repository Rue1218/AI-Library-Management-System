package com.library.library.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("category")
public class Category extends BaseEntity {
    @TableField("name")
    private String name;

    @TableField("parent_id")
    private Long parentId;

    @TableField("sort")
    private Integer sort;

    @TableField(exist = false)
    private Integer bookCount;
}