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
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private final TradeOrderMapper orderMapper;
    private final IdleProductMapper productMapper;
    private final TradeReviewMapper reviewMapper;
    private final PlatformUserMapper userMapper;
    private final ProductService productService;

    OrderService(TradeOrderMapper orderMapper,
                 IdleProductMapper productMapper,
                 TradeReviewMapper reviewMapper,
                 PlatformUserMapper userMapper,
                 ProductService productService) {
        this.orderMapper = orderMapper;
        this.productMapper = productMapper;
        this.reviewMapper = reviewMapper;
        this.userMapper = userMapper;
        this.productService = productService;
    }

    @Transactional
    public Map<String, Object> create(OrderCreateRequest request) {
        IdleProduct product = productService.requireProduct(request.productId);
        Long buyerId = UserContext.currentUserId();
        if (buyerId.equals(product.sellerId)) {
            throw new AppException("不能购买自己发布的商品");
        }
        if (!AuditState.APPROVED.name().equals(product.auditStatus) || !ProductState.AVAILABLE.name().equals(product.status)) {
            throw new AppException("商品当前不可下单");
        }
        TradeOrder order = new TradeOrder();
        order.orderNo = "OD" + DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now())
                + UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        order.productId = product.id;
        order.buyerId = buyerId;
        order.sellerId = product.sellerId;
        order.amount = product.price;
        order.status = OrderState.PENDING.name();
        order.createdAt = LocalDateTime.now();
        order.updatedAt = LocalDateTime.now();
        orderMapper.insert(order);

        product.status = ProductState.RESERVED.name();
        product.updatedAt = LocalDateTime.now();
        productMapper.updateById(product);
        return toOrderVO(order);
    }

    public List<Map<String, Object>> myOrders() {
        Long userId = UserContext.currentUserId();
        return orderMapper.selectList(new QueryWrapper<TradeOrder>()
                        .and(wrapper -> wrapper.eq("buyer_id", userId).or().eq("seller_id", userId))
                        .orderByDesc("created_at"))
                .stream().map(this::toOrderVO).collect(Collectors.toList());
    }

    public List<Map<String, Object>> allOrders() {
        return orderMapper.selectList(new QueryWrapper<TradeOrder>().orderByDesc("created_at"))
                .stream().map(this::toOrderVO).collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> cancel(Long orderId) {
        TradeOrder order = requireOrder(orderId);
        Long userId = UserContext.currentUserId();
        if (!userId.equals(order.buyerId) && !UserContext.isAdmin()) {
            throw new AppException("无权取消该订单");
        }
        order.status = OrderState.CANCELLED.name();
        order.updatedAt = LocalDateTime.now();
        orderMapper.updateById(order);

        IdleProduct product = productMapper.selectById(order.productId);
        if (product != null) {
            product.status = ProductState.AVAILABLE.name();
            product.updatedAt = LocalDateTime.now();
            productMapper.updateById(product);
        }
        return toOrderVO(order);
    }

    @Transactional
    public Map<String, Object> complete(Long orderId) {
        TradeOrder order = requireOrder(orderId);
        Long userId = UserContext.currentUserId();
        if (!userId.equals(order.buyerId) && !userId.equals(order.sellerId) && !UserContext.isAdmin()) {
            throw new AppException("无权完成该订单");
        }
        order.status = OrderState.COMPLETED.name();
        order.finishedAt = LocalDateTime.now();
        order.updatedAt = LocalDateTime.now();
        orderMapper.updateById(order);

        IdleProduct product = productMapper.selectById(order.productId);
        if (product != null) {
            product.status = ProductState.SOLD.name();
            product.updatedAt = LocalDateTime.now();
            productMapper.updateById(product);
        }
        return toOrderVO(order);
    }

    @Transactional
    public Map<String, Object> dispute(Long orderId, DisputeRequest request) {
        TradeOrder order = requireOrder(orderId);
        order.status = OrderState.DISPUTED.name();
        order.disputeReason = request.reason;
        order.updatedAt = LocalDateTime.now();
        orderMapper.updateById(order);
        return toOrderVO(order);
    }

    @Transactional
    public Map<String, Object> mediate(Long orderId, AuditRequest request) {
        TradeOrder order = requireOrder(orderId);
        if ("complete".equalsIgnoreCase(request.status)) {
            order.status = OrderState.COMPLETED.name();
            order.finishedAt = LocalDateTime.now();
            IdleProduct product = productMapper.selectById(order.productId);
            if (product != null) {
                product.status = ProductState.SOLD.name();
                productMapper.updateById(product);
            }
        } else if ("cancel".equalsIgnoreCase(request.status)) {
            order.status = OrderState.CANCELLED.name();
            IdleProduct product = productMapper.selectById(order.productId);
            if (product != null) {
                product.status = ProductState.AVAILABLE.name();
                productMapper.updateById(product);
            }
        } else {
            throw new AppException("处理状态仅支持 complete/cancel");
        }
        order.disputeReason = request.note;
        order.updatedAt = LocalDateTime.now();
        orderMapper.updateById(order);
        return toOrderVO(order);
    }

    @Transactional
    public Map<String, Object> review(Long orderId, ReviewRequest request) {
        TradeOrder order = requireOrder(orderId);
        if (!OrderState.COMPLETED.name().equals(order.status)) {
            throw new AppException("不能购买自己发布的商品");
        }
        Long currentUserId = UserContext.currentUserId();
        Long revieweeId = currentUserId.equals(order.buyerId) ? order.sellerId : order.buyerId;
        TradeReview existing = reviewMapper.selectOne(new QueryWrapper<TradeReview>()
                .eq("order_id", orderId).eq("reviewer_id", currentUserId).last("limit 1"));
        if (existing != null) {
            throw new AppException("你已经评价过该订单");
        }
        TradeReview review = new TradeReview();
        review.orderId = orderId;
        review.productId = order.productId;
        review.reviewerId = currentUserId;
        review.revieweeId = revieweeId;
        review.score = request.score;
        review.content = request.content;
        review.createdAt = LocalDateTime.now();
        reviewMapper.insert(review);

        Map<String, Object> result = new HashMap<>();
        result.put("reviewId", review.id);
        result.put("score", review.score);
        return result;
    }

    TradeOrder requireOrder(Long orderId) {
        TradeOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new AppException("订单不存在");
        }
        return order;
    }

    Map<String, Object> toOrderVO(TradeOrder order) {
        IdleProduct product = productMapper.selectById(order.productId);
        PlatformUser buyer = userMapper.selectById(order.buyerId);
        PlatformUser seller = userMapper.selectById(order.sellerId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", order.id);
        result.put("orderNo", order.orderNo);
        result.put("productId", order.productId);
        result.put("productTitle", product == null ? "-" : product.title);
        result.put("buyerId", order.buyerId);
        result.put("buyerName", buyer == null ? "-" : buyer.username);
        result.put("sellerId", order.sellerId);
        result.put("sellerName", seller == null ? "-" : seller.username);
        result.put("amount", order.amount);
        result.put("status", order.status);
        result.put("disputeReason", order.disputeReason);
        result.put("createdAt", order.createdAt);
        result.put("finishedAt", order.finishedAt);
        return result;
    }
}
