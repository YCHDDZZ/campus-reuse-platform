package com.campus.reuse.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("idle_product")
public class IdleProduct {
    @TableId(type = IdType.AUTO)
    public Long id;
    public String title;
    public String description;
    public BigDecimal price;
    public Long categoryId;
    public Long sellerId;
    public String status;
    public String auditStatus;
    public Integer donationEnabled;
    public String imagesJson;
    public String contactQrUrl;
    public Integer viewCount;
    public Integer favoriteCount;
    public Integer recycleScore;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
}
