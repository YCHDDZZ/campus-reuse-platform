package com.campus.reuse.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campus.reuse.dto.request.LoginRequest;
import com.campus.reuse.dto.request.ProfileUpdateRequest;
import com.campus.reuse.dto.request.RegisterRequest;
import com.campus.reuse.entity.PlatformUser;
import com.campus.reuse.enums.AuditState;
import com.campus.reuse.enums.RoleType;
import com.campus.reuse.exception.AppException;
import com.campus.reuse.mapper.PlatformUserMapper;
import com.campus.reuse.security.JwtTokenProvider;
import com.campus.reuse.security.UserContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

@Service
public class AuthService {
    private final PlatformUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(PlatformUserMapper userMapper, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public Map<String, Object> register(RegisterRequest request) {
        if (userMapper.selectCount(new QueryWrapper<PlatformUser>().eq("username", request.username)) > 0) {
            throw new AppException("Username already exists");
        }
        if (userMapper.selectCount(new QueryWrapper<PlatformUser>().eq("email", request.email)) > 0) {
            throw new AppException("Email already exists");
        }

        PlatformUser user = new PlatformUser();
        user.username = request.username;
        user.password = passwordEncoder.encode(request.password);
        user.realName = request.realName;
        user.email = request.email;
        user.phone = request.phone;
        user.avatarUrl = null;
        user.role = RoleType.USER.name();
        user.auditStatus = AuditState.PENDING.name();
        user.disabled = 0;
        user.createdAt = LocalDateTime.now();
        user.updatedAt = LocalDateTime.now();
        userMapper.insert(user);

        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.id);
        result.put("auditStatus", user.auditStatus);
        result.put("message", "Registration succeeded, please wait for admin approval");
        return result;
    }

    public Map<String, Object> login(LoginRequest request) {
        PlatformUser user = userMapper.selectOne(new QueryWrapper<PlatformUser>()
                .and(wrapper -> wrapper.eq("username", request.username).or().eq("email", request.username))
                .last("limit 1"));
        if (user == null || !passwordEncoder.matches(request.password, user.password)) {
            throw new AppException("Invalid username or password");
        }
        if (user.disabled != null && user.disabled == 1) {
            throw new AppException("Account has been disabled");
        }
        if (RoleType.USER.name().equals(user.role) && !AuditState.APPROVED.name().equals(user.auditStatus)) {
            throw new AppException("Invalid username or password");
        }
        Map<String, Object> result = buildUserProfile(user);
        result.put("token", jwtTokenProvider.createToken(user));
        return result;
    }

    public Map<String, Object> me() {
        PlatformUser user = userMapper.selectById(UserContext.currentUserId());
        return buildUserProfile(user);
    }

    @Transactional
    public Map<String, Object> updateProfile(ProfileUpdateRequest request) {
        PlatformUser user = userMapper.selectById(UserContext.currentUserId());
        if (user == null) {
            throw new AppException("User does not exist");
        }

        if (StringUtils.hasText(request.email) && !request.email.equals(user.email)) {
            long count = userMapper.selectCount(new QueryWrapper<PlatformUser>()
                    .eq("email", request.email)
                    .ne("id", user.id));
            if (count > 0) {
                throw new AppException("Email already exists");
            }
            user.email = request.email.trim();
        }
        if (StringUtils.hasText(request.realName)) {
            user.realName = request.realName.trim();
        }
        if (StringUtils.hasText(request.phone)) {
            user.phone = request.phone.trim();
        }
        if (request.avatarUrl != null) {
            user.avatarUrl = request.avatarUrl.trim();
        }
        user.updatedAt = LocalDateTime.now();
        userMapper.updateById(user);
        return buildUserProfile(user);
    }

    public Map<String, Object> mockSmsCode(String phone) {
        Map<String, Object> result = new HashMap<>();
        result.put("phone", phone);
        result.put("code", String.format("%06d", new Random().nextInt(999999)));
        result.put("message", "Demo only. SMS is not actually sent.");
        return result;
    }

    Map<String, Object> buildUserProfile(PlatformUser user) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", user.id);
        result.put("username", user.username);
        result.put("realName", user.realName);
        result.put("email", user.email);
        result.put("phone", user.phone);
        result.put("avatarUrl", user.avatarUrl);
        result.put("role", user.role);
        result.put("auditStatus", user.auditStatus);
        return result;
    }
}
