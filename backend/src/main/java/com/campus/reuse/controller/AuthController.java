package com.campus.reuse.controller;

import com.campus.reuse.common.ApiResponse;
import com.campus.reuse.dto.request.LoginRequest;
import com.campus.reuse.dto.request.ProfileUpdateRequest;
import com.campus.reuse.dto.request.RegisterRequest;
import com.campus.reuse.service.AuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ApiResponse<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.ok("Register succeeded", authService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok("Login succeeded", authService.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<Map<String, Object>> me() {
        return ApiResponse.ok(authService.me());
    }

    @PutMapping("/profile")
    public ApiResponse<Map<String, Object>> updateProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        return ApiResponse.ok("Profile updated", authService.updateProfile(request));
    }

    @PostMapping("/mock-sms-code")
    public ApiResponse<Map<String, Object>> mockSmsCode(@RequestParam String phone) {
        return ApiResponse.ok(authService.mockSmsCode(phone));
    }
}
