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

public class RegisterRequest {
    @NotBlank
    @Size(min = 4, max = 20)
    public String username;
    @NotBlank
    @Size(min = 6, max = 30)
    public String password;
    @NotBlank
    public String realName;
    @NotBlank
    @Email
    public String email;
    @NotBlank
    public String phone;
}
