package com.campus.reuse.controller;

import org.springframework.web.bind.annotation.*;
import com.campus.reuse.common.ApiResponse;
import com.campus.reuse.dto.request.*;
import com.campus.reuse.entity.*;
import com.campus.reuse.service.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminService adminService;
    private final ProductService productService;
    private final CategoryService categoryService;
    private final OrderService orderService;
    private final DonationService donationService;

    AdminController(AdminService adminService,
                    ProductService productService,
                    CategoryService categoryService,
                    OrderService orderService,
                    DonationService donationService) {
        this.adminService = adminService;
        this.productService = productService;
        this.categoryService = categoryService;
        this.orderService = orderService;
        this.donationService = donationService;
    }

    @GetMapping("/dashboard")
    public ApiResponse<Map<String, Object>> dashboard() {
        return ApiResponse.ok(adminService.dashboard());
    }

    @GetMapping("/users")
    public ApiResponse<List<Map<String, Object>>> users(@RequestParam(required = false) String auditStatus) {
        return ApiResponse.ok(adminService.users(auditStatus));
    }

    @PostMapping("/users/{id}/audit")
    public ApiResponse<Map<String, Object>> auditUser(@PathVariable Long id, @Valid @RequestBody AuditRequest request) {
        return ApiResponse.ok("用户审核完成", adminService.auditUser(id, request));
    }

    @GetMapping("/products/pending")
    public ApiResponse<List<Map<String, Object>>> pendingProducts() {
        return ApiResponse.ok(productService.pendingProducts());
    }

    @PostMapping("/products/{id}/audit")
    public ApiResponse<Map<String, Object>> auditProduct(@PathVariable Long id, @Valid @RequestBody AuditRequest request) {
        return ApiResponse.ok("商品审核完成", productService.audit(id, request));
    }

    @GetMapping("/reports")
    public ApiResponse<List<Map<String, Object>>> reports() {
        return ApiResponse.ok(adminService.reports());
    }

    @PostMapping("/reports/{id}/resolve")
    public ApiResponse<Map<String, Object>> resolve(@PathVariable Long id, @Valid @RequestBody AuditRequest request) {
        return ApiResponse.ok("举报处理完成", adminService.resolveReport(id, request));
    }

    @GetMapping("/orders")
    public ApiResponse<List<Map<String, Object>>> orders() {
        return ApiResponse.ok(orderService.allOrders());
    }

    @PostMapping("/orders/{id}/mediate")
    public ApiResponse<Map<String, Object>> mediate(@PathVariable Long id, @Valid @RequestBody AuditRequest request) {
        return ApiResponse.ok("纠纷处理完成", orderService.mediate(id, request));
    }

    @GetMapping("/donations")
    public ApiResponse<List<Map<String, Object>>> donations() {
        return ApiResponse.ok(donationService.allDonations());
    }

    @PostMapping("/donations/{id}/complete")
    public ApiResponse<Map<String, Object>> completeDonation(@PathVariable Long id, @Valid @RequestBody AuditRequest request) {
        return ApiResponse.ok("捐赠处理完成", donationService.complete(id, request));
    }

    @GetMapping("/categories")
    public ApiResponse<List<Category>> allCategories() {
        return ApiResponse.ok(categoryService.listAll());
    }

    @PostMapping("/categories")
    public ApiResponse<Category> createCategory(@Valid @RequestBody CategoryRequest request) {
        return ApiResponse.ok("分类创建成功", categoryService.save(request));
    }

    @PutMapping("/categories/{id}")
    public ApiResponse<Category> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        return ApiResponse.ok("分类更新成功", categoryService.update(id, request));
    }

    @DeleteMapping("/categories/{id}")
    public ApiResponse<Object> deleteCategory(@PathVariable Long id) {
        categoryService.delete(id);
        return ApiResponse.ok("分类删除成功", null);
    }

    // 管理员修改商品内容
    @PutMapping("/products/{id}")
    public ApiResponse<Map<String, Object>> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductCreateRequest request) {
        return ApiResponse.ok("商品更新成功", productService.updateProductByAdmin(id, request));
    }

    // 管理员冻结商品
    @PostMapping("/products/{id}/freeze")
    public ApiResponse<Map<String, Object>> freezeProduct(@PathVariable Long id, @Valid @RequestBody AuditRequest request) {
        return ApiResponse.ok("商品已冻结", productService.freezeProduct(id, request));
    }

    // 管理员解冻商品
    @PostMapping("/products/{id}/unfreeze")
    public ApiResponse<Map<String, Object>> unfreezeProduct(@PathVariable Long id, @Valid @RequestBody AuditRequest request) {
        return ApiResponse.ok("商品已解冻", productService.unfreezeProduct(id, request));
    }

    // 管理员下架商品
    @PostMapping("/products/{id}/offline")
    public ApiResponse<Map<String, Object>> offlineProduct(@PathVariable Long id, @Valid @RequestBody AuditRequest request) {
        return ApiResponse.ok("商品已下架", productService.offlineProduct(id, request));
    }

    // 管理员重新上架商品
    @PostMapping("/products/{id}/online")
    public ApiResponse<Map<String, Object>> onlineProduct(@PathVariable Long id, @Valid @RequestBody AuditRequest request) {
        return ApiResponse.ok("商品已上架", productService.onlineProduct(id, request));
    }
}
