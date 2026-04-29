package com.campus.reuse.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.campus.reuse.dto.request.*;
import com.campus.reuse.entity.*;
import com.campus.reuse.enums.*;
import com.campus.reuse.exception.AppException;
import com.campus.reuse.mapper.*;
import com.campus.reuse.security.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private final IdleProductMapper productMapper;
    private final PlatformUserMapper userMapper;
    private final CategoryMapper categoryMapper;
    private final ProductFavoriteMapper favoriteMapper;
    private final ProductMessageMapper messageMapper;
    private final ProductReportMapper reportMapper;
    private final TradeReviewMapper reviewMapper;
    private final ObjectMapper objectMapper;

    ProductService(IdleProductMapper productMapper,
                   PlatformUserMapper userMapper,
                   CategoryMapper categoryMapper,
                   ProductFavoriteMapper favoriteMapper,
                   ProductMessageMapper messageMapper,
                   ProductReportMapper reportMapper,
                   TradeReviewMapper reviewMapper,
                   ObjectMapper objectMapper) {
        this.productMapper = productMapper;
        this.userMapper = userMapper;
        this.categoryMapper = categoryMapper;
        this.favoriteMapper = favoriteMapper;
        this.messageMapper = messageMapper;
        this.reportMapper = reportMapper;
        this.reviewMapper = reviewMapper;
        this.objectMapper = objectMapper;
    }

    @Cacheable(cacheNames = "product-search", key = "#request.keyword + ':' + #request.categoryId + ':' + #request.sortBy + ':' + #request.auditStatus")
    public List<Map<String, Object>> search(ProductSearchRequest request) {
        QueryWrapper<IdleProduct> wrapper = new QueryWrapper<IdleProduct>();

        // 如果指定了auditStatus参数，则使用该参数，否则默认只查询已审核通过的商品
        if (request.auditStatus != null && !request.auditStatus.trim().isEmpty()) {
            wrapper.eq("audit_status", request.auditStatus);
        } else {
            wrapper.eq("audit_status", AuditState.APPROVED.name());
        }

        // 对于已审核的商品，默认排除离线和审核中的状态的商品
        if (request.auditStatus == null || AuditState.APPROVED.name().equals(request.auditStatus)) {
            wrapper.ne("status", ProductState.OFFLINE.name())
                   .ne("status", ProductState.AUDITING.name()); // 不显示正在审核中的商品给普通用户
        }

        if (request.keyword != null && !request.keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like("title", request.keyword).or().like("description", request.keyword));
        }
        if (request.categoryId != null) {
            wrapper.eq("category_id", request.categoryId);
        }
        if ("priceAsc".equalsIgnoreCase(request.sortBy)) {
            wrapper.orderByAsc("price");
        } else if ("hot".equalsIgnoreCase(request.sortBy)) {
            wrapper.orderByDesc("favorite_count", "view_count");
        } else {
            wrapper.orderByDesc("created_at");
        }
        return productMapper.selectList(wrapper).stream().map(product -> toProductCard(product, null)).collect(Collectors.toList());
    }

    public List<Map<String, Object>> myProducts() {
        Long userId = UserContext.currentUserId();
        return productMapper.selectList(new QueryWrapper<IdleProduct>()
                        .eq("seller_id", userId)
                        .orderByDesc("created_at"))
                .stream().map(product -> toProductCard(product, userId)).collect(Collectors.toList());
    }

    public Map<String, Object> detail(Long id) {
        IdleProduct product = requireProduct(id);
        product.viewCount = product.viewCount == null ? 1 : product.viewCount + 1;
        product.updatedAt = LocalDateTime.now();
        productMapper.updateById(product);
        Long currentUserId = null;
        try {
            currentUserId = UserContext.currentUserId();
        } catch (Exception ignored) {
        }
        Map<String, Object> detail = toProductCard(product, currentUserId);
        detail.put("messages", listMessages(id));
        detail.put("reviews", listReviews(id));
        return detail;
    }

    @Transactional
    @CacheEvict(cacheNames = "product-search", allEntries = true)
    public Map<String, Object> create(ProductCreateRequest request) {
        Category category = categoryMapper.selectById(request.categoryId);
        if (category == null || category.enabled != 1) {
            throw new AppException("分类不存在或已停用");
        }
        IdleProduct product = new IdleProduct();
        product.title = request.title;
        product.description = request.description;
        product.price = request.price;
        product.categoryId = request.categoryId;
        product.sellerId = UserContext.currentUserId();
        product.status = ProductState.DRAFT.name();
        product.auditStatus = AuditState.PENDING.name();
        product.donationEnabled = request.donationEnabled == null ? 0 : request.donationEnabled;
        product.contactQrUrl = request.contactQrUrl;
        product.imagesJson = writeImages(request.images);
        product.viewCount = 0;
        product.favoriteCount = 0;
        product.recycleScore = calculateRecycleScore(request);
        product.createdAt = LocalDateTime.now();
        product.updatedAt = LocalDateTime.now();
        productMapper.insert(product);
        return toProductCard(product, product.sellerId);
    }

    @Transactional
    @CacheEvict(cacheNames = "product-search", allEntries = true)
    public Map<String, Object> update(Long id, ProductCreateRequest request) {
        IdleProduct product = requireProduct(id);
        if (!product.sellerId.equals(UserContext.currentUserId())) {
            throw new AppException("无权修改该商品");
        }

        // 保存原始状态，以便在审核后恢复
        String originalStatus = product.status;

        product.title = request.title;
        product.description = request.description;
        product.price = request.price;
        product.categoryId = request.categoryId;
        product.donationEnabled = request.donationEnabled == null ? 0 : request.donationEnabled;
        product.contactQrUrl = request.contactQrUrl;
        product.imagesJson = writeImages(request.images);
        product.auditStatus = AuditState.PENDING.name();

        // 如果商品原来是可用状态，在用户更新后将其设为审核中状态，这样商品在审核期间仍然可见
        if (ProductState.AVAILABLE.name().equals(originalStatus) ||
            ProductState.RESERVED.name().equals(originalStatus) ||
            ProductState.DONATING.name().equals(originalStatus) ||
            ProductState.AUDITING.name().equals(originalStatus)) { // 如果已经是审核中状态
            product.status = ProductState.AUDITING.name();
        } else {
            // 对于其他状态，如DRAFT, SOLD, OFFLINE等，可以保持原来的行为
            product.status = ProductState.DRAFT.name();
        }

        product.recycleScore = calculateRecycleScore(request);
        product.updatedAt = LocalDateTime.now();
        productMapper.updateById(product);

        // 将原始状态存储到额外字段中，便于审核时参考（如果数据库表结构允许）
        // 或者在业务逻辑中临时记录
        return toProductCard(product, product.sellerId);
    }

    @Transactional
    public Map<String, Object> favorite(Long productId, boolean enabled) {
        Long userId = UserContext.currentUserId();
        ProductFavorite favorite = favoriteMapper.selectOne(new QueryWrapper<ProductFavorite>()
                .eq("user_id", userId)
                .eq("product_id", productId)
                .last("limit 1"));
        IdleProduct product = requireProduct(productId);
        if (enabled && favorite == null) {
            favorite = new ProductFavorite();
            favorite.userId = userId;
            favorite.productId = productId;
            favorite.createdAt = LocalDateTime.now();
            favoriteMapper.insert(favorite);
            product.favoriteCount = (product.favoriteCount == null ? 0 : product.favoriteCount) + 1;
            productMapper.updateById(product);
        }
        if (!enabled && favorite != null) {
            favoriteMapper.deleteById(favorite.id);
            product.favoriteCount = Math.max(0, (product.favoriteCount == null ? 0 : product.favoriteCount) - 1);
            productMapper.updateById(product);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("favorite", enabled);
        result.put("favoriteCount", product.favoriteCount);
        return result;
    }

    public List<Map<String, Object>> myFavorites() {
        Long userId = UserContext.currentUserId();
        List<ProductFavorite> favorites = favoriteMapper.selectList(new QueryWrapper<ProductFavorite>()
                .eq("user_id", userId)
                .orderByDesc("created_at"));
        List<Map<String, Object>> result = new ArrayList<>();
        for (ProductFavorite favorite : favorites) {
            IdleProduct product = productMapper.selectById(favorite.productId);
            if (product != null) {
                result.add(toProductCard(product, userId));
            }
        }
        return result;
    }

    @Transactional
    public Map<String, Object> leaveMessage(Long productId, MessageRequest request) {
        IdleProduct product = requireProduct(productId);
        ProductMessage message = new ProductMessage();
        message.productId = productId;
        message.fromUserId = UserContext.currentUserId();
        message.toUserId = request.toUserId == null ? product.sellerId : request.toUserId;
        message.content = request.content;
        message.readFlag = 0;
        message.createdAt = LocalDateTime.now();
        messageMapper.insert(message);
        return toMessageVO(message);
    }

    public List<Map<String, Object>> listMessages(Long productId) {
        return messageMapper.selectList(new QueryWrapper<ProductMessage>()
                        .eq("product_id", productId)
                        .orderByAsc("created_at"))
                .stream().map(this::toMessageVO).collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> report(Long productId, ReportRequest request) {
        requireProduct(productId);
        ProductReport report = new ProductReport();
        report.productId = productId;
        report.reporterId = UserContext.currentUserId();
        report.reason = request.reason;
        report.status = ReportState.PENDING.name();
        report.createdAt = LocalDateTime.now();
        report.updatedAt = LocalDateTime.now();
        reportMapper.insert(report);
        Map<String, Object> result = new HashMap<>();
        result.put("reportId", report.id);
        result.put("status", report.status);
        return result;
    }

    public List<Map<String, Object>> pendingProducts() {
        return productMapper.selectList(new QueryWrapper<IdleProduct>()
                        .eq("audit_status", AuditState.PENDING.name())
                        .orderByDesc("created_at"))
                .stream().map(product -> toProductCard(product, null)).collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(cacheNames = "product-search", allEntries = true)
    public Map<String, Object> audit(Long productId, AuditRequest request) {
        IdleProduct product = requireProduct(productId);
        if (!List.of(AuditState.APPROVED.name(), AuditState.REJECTED.name()).contains(request.status)) {
            throw new AppException("审核状态不合法");
        }

        product.auditStatus = request.status;

        // 根据审核结果和商品原有状态设置新状态
        if (AuditState.APPROVED.name().equals(request.status)) {
            // 审核通过
            if (ProductState.AUDITING.name().equals(product.status)) {
                // 如果商品之前是在审核中，恢复到合适的可用状态
                // 通常情况下，从AUDITING状态审核通过后应变为AVAILABLE
                product.status = ProductState.AVAILABLE.name();
            } else {
                // 其他情况，按原有逻辑处理
                product.status = ProductState.AVAILABLE.name();
            }
        } else {
            // 审核拒绝
            product.status = ProductState.OFFLINE.name();
        }

        product.updatedAt = LocalDateTime.now();
        productMapper.updateById(product);
        return toProductCard(product, null);
    }

    public List<Map<String, Object>> listReviews(Long productId) {
        return reviewMapper.selectList(new QueryWrapper<TradeReview>()
                        .eq("product_id", productId)
                        .orderByDesc("created_at"))
                .stream().map(review -> {
                    PlatformUser reviewer = userMapper.selectById(review.reviewerId);
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", review.id);
                    map.put("score", review.score);
                    map.put("content", review.content);
                    map.put("reviewer", reviewer == null ? "匿名用户" : reviewer.username);
                    map.put("createdAt", review.createdAt);
                    return map;
                }).collect(Collectors.toList());
    }

    IdleProduct requireProduct(Long id) {
        IdleProduct product = productMapper.selectById(id);
        if (product == null) {
            throw new AppException("商品不存在");
        }
        return product;
    }

    Map<String, Object> toProductCard(IdleProduct product, Long currentUserId) {
        Map<String, Object> result = new LinkedHashMap<>();
        Category category = categoryMapper.selectById(product.categoryId);
        PlatformUser seller = userMapper.selectById(product.sellerId);
        result.put("id", product.id);
        result.put("title", product.title);
        result.put("description", product.description);
        result.put("price", product.price);
        result.put("categoryId", product.categoryId);
        result.put("categoryName", category == null ? "-" : category.name);
        result.put("sellerId", product.sellerId);
        result.put("sellerName", seller == null ? "-" : seller.username);
        result.put("status", product.status);
        result.put("auditStatus", product.auditStatus);
        result.put("viewCount", product.viewCount);
        result.put("favoriteCount", product.favoriteCount);
        result.put("recycleScore", product.recycleScore);
        result.put("donationEnabled", product.donationEnabled);
        result.put("contactQrUrl", product.contactQrUrl);
        result.put("images", readImages(product.imagesJson));
        result.put("createdAt", product.createdAt);
        result.put("updatedAt", product.updatedAt);
        if (currentUserId != null) {
            result.put("favorite", favoriteMapper.selectCount(new QueryWrapper<ProductFavorite>()
                    .eq("user_id", currentUserId).eq("product_id", product.id)) > 0);
        } else {
            result.put("favorite", false);
        }
        return result;
    }

    Map<String, Object> toMessageVO(ProductMessage message) {
        PlatformUser fromUser = userMapper.selectById(message.fromUserId);
        PlatformUser toUser = userMapper.selectById(message.toUserId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", message.id);
        result.put("productId", message.productId);
        result.put("fromUserId", message.fromUserId);
        result.put("fromUserName", fromUser == null ? "-" : fromUser.username);
        result.put("toUserId", message.toUserId);
        result.put("toUserName", toUser == null ? "-" : toUser.username);
        result.put("content", message.content);
        result.put("createdAt", message.createdAt);
        return result;
    }

    private Integer calculateRecycleScore(ProductCreateRequest request) {
        int score = 10;
        score += Math.min(request.title.length(), 20);
        score += request.donationEnabled != null && request.donationEnabled == 1 ? 15 : 0;
        score += request.images == null ? 0 : Math.min(request.images.size() * 5, 20);
        return score;
    }

    private String writeImages(List<String> images) {
        try {
            return objectMapper.writeValueAsString(images == null ? Collections.emptyList() : images);
        } catch (Exception e) {
            return "[]";
        }
    }

    private List<String> readImages(String value) {
        try {
            if (value == null || value.trim().isEmpty()) {
                return Collections.emptyList();
            }
            return objectMapper.readValue(value, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    // 管理员修改商品内容
    @Transactional
    @CacheEvict(cacheNames = "product-search", allEntries = true)
    public Map<String, Object> updateProductByAdmin(Long id, ProductCreateRequest request) {
        IdleProduct product = requireProduct(id);

        // 验证分类是否存在且启用
        Category category = categoryMapper.selectById(request.categoryId);
        if (category == null || category.enabled != 1) {
            throw new AppException("分类不存在或已停用");
        }

        // 更新商品信息
        product.title = request.title;
        product.description = request.description;
        product.price = request.price;
        product.categoryId = request.categoryId;
        product.donationEnabled = request.donationEnabled == null ? 0 : request.donationEnabled;
        product.contactQrUrl = request.contactQrUrl;
        product.imagesJson = writeImages(request.images);
        product.recycleScore = calculateRecycleScore(request);
        product.updatedAt = LocalDateTime.now();

        // 如果商品之前是冻结状态，更新后继续保持原状态
        // 如果商品之前是下架状态，更新后恢复到可用状态
        if (ProductState.OFFLINE.name().equals(product.status) ||
            ProductState.FROZEN.name().equals(product.status)) {
            // 保持原有状态
        } else {
            // 如果是审核通过的上架商品，更新后仍然保持可用状态
            if (AuditState.APPROVED.name().equals(product.auditStatus)) {
                product.status = ProductState.AVAILABLE.name();
            }
        }

        productMapper.updateById(product);
        return toProductCard(product, null);
    }

    // 管理员冻结商品
    @Transactional
    @CacheEvict(cacheNames = "product-search", allEntries = true)
    public Map<String, Object> freezeProduct(Long id, AuditRequest request) {
        IdleProduct product = requireProduct(id);

        // 只有审核通过的商品才能被冻结
        if (!AuditState.APPROVED.name().equals(product.auditStatus)) {
            throw new AppException("只有审核通过的商品才能被冻结");
        }

        product.status = ProductState.FROZEN.name(); // 需要添加FROZEN状态到ProductState枚举
        product.updatedAt = LocalDateTime.now();
        productMapper.updateById(product);

        return toProductCard(product, null);
    }

    // 管理员解冻商品
    @Transactional
    @CacheEvict(cacheNames = "product-search", allEntries = true)
    public Map<String, Object> unfreezeProduct(Long id, AuditRequest request) {
        IdleProduct product = requireProduct(id);

        // 只有冻结状态的商品才能被解冻
        if (!ProductState.FROZEN.name().equals(product.status)) {
            throw new AppException("只有冻结状态的商品才能被解冻");
        }

        // 解冻后商品状态变为可用
        product.status = ProductState.AVAILABLE.name();
        product.updatedAt = LocalDateTime.now();
        productMapper.updateById(product);

        return toProductCard(product, null);
    }

    // 管理员下架商品
    @Transactional
    @CacheEvict(cacheNames = "product-search", allEntries = true)
    public Map<String, Object> offlineProduct(Long id, AuditRequest request) {
        IdleProduct product = requireProduct(id);

        product.status = ProductState.OFFLINE.name();
        product.updatedAt = LocalDateTime.now();
        productMapper.updateById(product);

        return toProductCard(product, null);
    }

    // 管理员重新上架商品
    @Transactional
    @CacheEvict(cacheNames = "product-search", allEntries = true)
    public Map<String, Object> onlineProduct(Long id, AuditRequest request) {
        IdleProduct product = requireProduct(id);

        // 重新上架的商品必须是审核通过的
        if (!AuditState.APPROVED.name().equals(product.auditStatus)) {
            throw new AppException("只有审核通过的商品才能重新上架");
        }

        product.status = ProductState.AVAILABLE.name();
        product.updatedAt = LocalDateTime.now();
        productMapper.updateById(product);

        return toProductCard(product, null);
    }
}
