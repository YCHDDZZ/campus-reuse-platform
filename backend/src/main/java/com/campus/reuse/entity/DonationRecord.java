package com.campus.reuse.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("donation_record")
public class DonationRecord {
    @TableId(type = IdType.AUTO)
    public Long id;
    public Long productId;
    public Long donorId;
    public String organization;
    public String status;
    public String receivedNote;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
}
