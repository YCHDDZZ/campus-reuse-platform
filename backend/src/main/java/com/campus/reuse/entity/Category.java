package com.campus.reuse.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("category")
public class Category {
    @TableId(type = IdType.AUTO)
    public Long id;
    public String name;
    public Integer sortNo;
    public Integer enabled;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
}
