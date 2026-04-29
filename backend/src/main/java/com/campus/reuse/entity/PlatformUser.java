package com.campus.reuse.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("platform_user")
public class PlatformUser {
    @TableId(type = IdType.AUTO)
    public Long id;
    public String username;
    public String password;
    public String realName;
    public String email;
    public String phone;
    public String avatarUrl;
    public String role;
    public String auditStatus;
    public Integer disabled;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
}
