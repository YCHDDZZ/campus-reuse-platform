package com.campus.reuse.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("product_report")
public class ProductReport {
    @TableId(type = IdType.AUTO)
    public Long id;
    public Long productId;
    public Long reporterId;
    public String reason;
    public String status;
    public String handlerNote;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
}
