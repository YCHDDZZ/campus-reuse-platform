package com.campus.reuse.dto.request;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

public class ProductCreateRequest {
    @NotBlank
    public String title;
    @NotBlank
    public String description;
    @NotNull
    @DecimalMin("0.01")
    public BigDecimal price;
    @NotNull
    public Long categoryId;
    public Integer donationEnabled = 0;
    public String contactQrUrl;
    public List<String> images;
}
