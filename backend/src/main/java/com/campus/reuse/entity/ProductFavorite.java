package com.campus.reuse.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("product_favorite")
public class ProductFavorite {
    @TableId(type = IdType.AUTO)
    public Long id;
    public Long userId;
    public Long productId;
    public LocalDateTime createdAt;
}
