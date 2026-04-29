package com.campus.reuse.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("trade_review")
public class TradeReview {
    @TableId(type = IdType.AUTO)
    public Long id;
    public Long orderId;
    public Long productId;
    public Long reviewerId;
    public Long revieweeId;
    public Integer score;
    public String content;
    public LocalDateTime createdAt;
}
