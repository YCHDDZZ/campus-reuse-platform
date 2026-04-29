package com.campus.reuse.dto.request;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

public class ProfileUpdateRequest {
    @Size(max = 50)
    public String realName;

    @Email
    @Size(max = 100)
    public String email;

    @Size(max = 20)
    public String phone;

    @Size(max = 255)
    public String avatarUrl;
}
