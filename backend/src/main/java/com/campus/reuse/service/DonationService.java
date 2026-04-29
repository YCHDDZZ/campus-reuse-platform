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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DonationService {
    private final DonationRecordMapper donationMapper;
    private final IdleProductMapper productMapper;
    private final ProductService productService;

    DonationService(DonationRecordMapper donationMapper, IdleProductMapper productMapper, ProductService productService) {
        this.donationMapper = donationMapper;
        this.productMapper = productMapper;
        this.productService = productService;
    }

    @Transactional
    public Map<String, Object> create(DonationRequest request) {
        IdleProduct product = productService.requireProduct(request.productId);
        if (!product.sellerId.equals(UserContext.currentUserId())) {
            throw new AppException("仅商品发布者可发起捐赠");
        }
        if (product.donationEnabled == null || product.donationEnabled != 1) {
            throw new AppException("当前商品未开启捐赠");
        }
        DonationRecord record = new DonationRecord();
        record.productId = request.productId;
        record.donorId = UserContext.currentUserId();
        record.organization = request.organization;
        record.status = DonationState.PENDING.name();
        record.createdAt = LocalDateTime.now();
        record.updatedAt = LocalDateTime.now();
        donationMapper.insert(record);

        product.status = ProductState.DONATING.name();
        product.updatedAt = LocalDateTime.now();
        productMapper.updateById(product);
        return toDonationVO(record);
    }

    public List<Map<String, Object>> myDonations() {
        return donationMapper.selectList(new QueryWrapper<DonationRecord>()
                        .eq("donor_id", UserContext.currentUserId())
                        .orderByDesc("created_at"))
                .stream().map(this::toDonationVO).collect(Collectors.toList());
    }

    public List<Map<String, Object>> allDonations() {
        return donationMapper.selectList(new QueryWrapper<DonationRecord>().orderByDesc("created_at"))
                .stream().map(this::toDonationVO).collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> complete(Long id, AuditRequest request) {
        DonationRecord record = donationMapper.selectById(id);
        if (record == null) {
            throw new AppException("捐赠记录不存在");
        }
        if (!List.of(DonationState.COMPLETED.name(), DonationState.REJECTED.name()).contains(request.status)) {
            throw new AppException("捐赠状态不合法");
        }
        record.status = request.status;
        record.receivedNote = request.note;
        record.updatedAt = LocalDateTime.now();
        donationMapper.updateById(record);

        IdleProduct product = productMapper.selectById(record.productId);
        if (product != null) {
            product.status = DonationState.COMPLETED.name().equals(request.status) ? ProductState.SOLD.name() : ProductState.AVAILABLE.name();
            product.updatedAt = LocalDateTime.now();
            productMapper.updateById(product);
        }
        return toDonationVO(record);
    }

    Map<String, Object> toDonationVO(DonationRecord record) {
        IdleProduct product = productMapper.selectById(record.productId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", record.id);
        result.put("productId", record.productId);
        result.put("productTitle", product == null ? "-" : product.title);
        result.put("organization", record.organization);
        result.put("status", record.status);
        result.put("receivedNote", record.receivedNote);
        result.put("createdAt", record.createdAt);
        return result;
    }
}
