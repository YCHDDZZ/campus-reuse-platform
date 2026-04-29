package com.campus.reuse.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("trade_order")
public class TradeOrder {
    @TableId(type = IdType.AUTO)
    public Long id;
    public String orderNo;
    public Long productId;
    public Long buyerId;
    public Long sellerId;
    public BigDecimal amount;
    public String status;
    public String disputeReason;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
    public LocalDateTime finishedAt;
}
