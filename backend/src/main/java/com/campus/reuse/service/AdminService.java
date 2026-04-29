package com.campus.reuse.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.campus.reuse.dto.request.*;
import com.campus.reuse.entity.*;
import com.campus.reuse.enums.*;
import com.campus.reuse.exception.AppException;
import com.campus.reuse.mapper.*;
import com.campus.reuse.security.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminService {
    private final PlatformUserMapper userMapper;
    private final ProductReportMapper reportMapper;
    private final CategoryService categoryService;
    private final IdleProductMapper productMapper;
    private final TradeOrderMapper orderMapper;
    private final DonationRecordMapper donationMapper;

    AdminService(PlatformUserMapper userMapper,
                 ProductReportMapper reportMapper,
                 CategoryService categoryService,
                 IdleProductMapper productMapper,
                 TradeOrderMapper orderMapper,
                 DonationRecordMapper donationMapper) {
        this.userMapper = userMapper;
        this.reportMapper = reportMapper;
        this.categoryService = categoryService;
        this.productMapper = productMapper;
        this.orderMapper = orderMapper;
        this.donationMapper = donationMapper;
    }

    public Map<String, Object> dashboard() {
        long totalUsers = userMapper.selectCount(null);
        long pendingUsers = userMapper.selectCount(new QueryWrapper<PlatformUser>().eq("audit_status", AuditState.PENDING.name()));
        long totalProducts = productMapper.selectCount(null);
        long pendingProducts = productMapper.selectCount(new QueryWrapper<IdleProduct>().eq("audit_status", AuditState.PENDING.name()));
        long totalOrders = orderMapper.selectCount(null);
        long disputeOrders = orderMapper.selectCount(new QueryWrapper<TradeOrder>().eq("status", OrderState.DISPUTED.name()));
        long completedDonations = donationMapper.selectCount(new QueryWrapper<DonationRecord>().eq("status", DonationState.COMPLETED.name()));
        long completedOrders = orderMapper.selectCount(new QueryWrapper<TradeOrder>().eq("status", OrderState.COMPLETED.name()));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalUsers", totalUsers);
        result.put("pendingUsers", pendingUsers);
        result.put("totalProducts", totalProducts);
        result.put("pendingProducts", pendingProducts);
        result.put("totalOrders", totalOrders);
        result.put("disputeOrders", disputeOrders);
        result.put("completedDonations", completedDonations);
        result.put("cycleIndex", completedOrders * 2 + completedDonations * 3 + totalProducts);
        result.put("categories", categoryService.listAll());
        return result;
    }

    public List<Map<String, Object>> users(String auditStatus) {
        QueryWrapper<PlatformUser> wrapper = new QueryWrapper<PlatformUser>().orderByDesc("created_at");
        if (auditStatus != null && !auditStatus.isBlank()) {
            wrapper.eq("audit_status", auditStatus);
        }
        return userMapper.selectList(wrapper).stream().map(user -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", user.id);
            map.put("username", user.username);
            map.put("realName", user.realName);
            map.put("email", user.email);
            map.put("phone", user.phone);
            map.put("avatarUrl", user.avatarUrl);
            map.put("role", user.role);
            map.put("auditStatus", user.auditStatus);
            map.put("disabled", user.disabled);
            map.put("createdAt", user.createdAt);
            return map;
        }).collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> auditUser(Long id, AuditRequest request) {
        PlatformUser user = userMapper.selectById(id);
        if (user == null) {
            throw new AppException("用户不存在");
        }
        if (!List.of(AuditState.APPROVED.name(), AuditState.REJECTED.name()).contains(request.status)) {
            throw new AppException("审核状态不合法");
        }
        user.auditStatus = request.status;
        user.updatedAt = LocalDateTime.now();
        userMapper.updateById(user);
        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.id);
        result.put("auditStatus", user.auditStatus);
        return result;
    }

    public List<Map<String, Object>> reports() {
        return reportMapper.selectList(new QueryWrapper<ProductReport>().orderByDesc("created_at"))
                .stream().map(report -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", report.id);
                    map.put("productId", report.productId);
                    map.put("reporterId", report.reporterId);
                    map.put("reason", report.reason);
                    map.put("status", report.status);
                    map.put("handlerNote", report.handlerNote);
                    map.put("createdAt", report.createdAt);
                    return map;
                }).collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> resolveReport(Long id, AuditRequest request) {
        ProductReport report = reportMapper.selectById(id);
        if (report == null) {
            throw new AppException("举报记录不存在");
        }
        if (!List.of(ReportState.RESOLVED.name(), ReportState.REJECTED.name()).contains(request.status)) {
            throw new AppException("举报处理状态不合法");
        }
        report.status = request.status;
        report.handlerNote = request.note;
        report.updatedAt = LocalDateTime.now();
        reportMapper.updateById(report);
        Map<String, Object> result = new HashMap<>();
        result.put("reportId", report.id);
        result.put("status", report.status);
        return result;
    }
}
