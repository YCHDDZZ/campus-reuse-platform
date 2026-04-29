package com.campus.reuse.init;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import com.campus.reuse.entity.*;
import com.campus.reuse.enums.*;
import com.campus.reuse.mapper.*;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DemoDataInitializer {
    private final PlatformUserMapper userMapper;
    private final CategoryMapper categoryMapper;
    private final IdleProductMapper productMapper;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    DemoDataInitializer(PlatformUserMapper userMapper,
                        CategoryMapper categoryMapper,
                        IdleProductMapper productMapper,
                        PasswordEncoder passwordEncoder,
                        ObjectMapper objectMapper) {
        this.userMapper = userMapper;
        this.categoryMapper = categoryMapper;
        this.productMapper = productMapper;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
    }

    @EventListener(org.springframework.boot.context.event.ApplicationReadyEvent.class)
    public void seed() {
        if (userMapper.selectCount(null) == 0) {
            PlatformUser admin = createUser("admin", "Admin@123", "平台管理员", "admin@campus.local", "13800000000", RoleType.ADMIN.name(), AuditState.APPROVED.name());
            PlatformUser student = createUser("student", "123456", "学生用户", "student@campus.local", "13900000001", RoleType.USER.name(), AuditState.APPROVED.name());
            PlatformUser seller = createUser("seller", "123456", "闲置卖家", "seller@campus.local", "13900000002", RoleType.USER.name(), AuditState.APPROVED.name());
            userMapper.insert(admin);
            userMapper.insert(student);
            userMapper.insert(seller);
        }
        if (categoryMapper.selectCount(null) == 0) {
            insertCategory("教材书籍", 1);
            insertCategory("数码电子", 2);
            insertCategory("生活用品", 3);
            insertCategory("体育器材", 4);
        }
        if (productMapper.selectCount(null) == 0) {
            PlatformUser seller = userMapper.selectOne(new QueryWrapper<PlatformUser>().eq("username", "seller").last("limit 1"));
            List<Category> categories = categoryMapper.selectList(new QueryWrapper<Category>().orderByAsc("sort_no"));
            insertProduct("九成新 Java Web 开发教材", "适合大三课程复习，带课堂笔记。", new BigDecimal("18.00"), categories.get(0).id, seller.id, 0);
            insertProduct("校园代步小风扇", "宿舍可用，静音节能。", new BigDecimal("25.00"), categories.get(2).id, seller.id, 1);
            insertProduct("羽毛球拍一副", "适合日常锻炼。", new BigDecimal("46.00"), categories.get(3).id, seller.id, 0);
        }
    }

    private PlatformUser createUser(String username, String password, String realName, String email, String phone, String role, String auditStatus) {
        PlatformUser user = new PlatformUser();
        user.username = username;
        user.password = passwordEncoder.encode(password);
        user.realName = realName;
        user.email = email;
        user.phone = phone;
        user.avatarUrl = "";
        user.role = role;
        user.auditStatus = auditStatus;
        user.disabled = 0;
        user.createdAt = LocalDateTime.now();
        user.updatedAt = LocalDateTime.now();
        return user;
    }

    private void insertCategory(String name, int sortNo) {
        Category category = new Category();
        category.name = name;
        category.sortNo = sortNo;
        category.enabled = 1;
        category.createdAt = LocalDateTime.now();
        category.updatedAt = LocalDateTime.now();
        categoryMapper.insert(category);
    }

    private void insertProduct(String title, String description, BigDecimal price, Long categoryId, Long sellerId, int donationEnabled) {
        IdleProduct product = new IdleProduct();
        product.title = title;
        product.description = description;
        product.price = price;
        product.categoryId = categoryId;
        product.sellerId = sellerId;
        product.status = ProductState.AVAILABLE.name();
        product.auditStatus = AuditState.APPROVED.name();
        product.donationEnabled = donationEnabled;
        product.contactQrUrl = "";
        try {
            product.imagesJson = objectMapper.writeValueAsString(List.of(
                    "https://dummyimage.com/400x260/5b8ff9/ffffff&text=" + java.net.URLEncoder.encode(title, StandardCharsets.UTF_8)
            ));
        } catch (Exception e) {
            product.imagesJson = "[]";
        }
        product.viewCount = 10;
        product.favoriteCount = 2;
        product.recycleScore = 25;
        product.createdAt = LocalDateTime.now();
        product.updatedAt = LocalDateTime.now();
        productMapper.insert(product);
    }
}
