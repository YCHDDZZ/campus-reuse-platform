package com.campus.reuse.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("product_message")
public class ProductMessage {
    @TableId(type = IdType.AUTO)
    public Long id;
    public Long productId;
    public Long fromUserId;
    public Long toUserId;
    public String content;
    public Integer readFlag;
    public LocalDateTime createdAt;
}
