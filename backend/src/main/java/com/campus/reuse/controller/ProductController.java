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
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> search(ProductSearchRequest request) {
        return ApiResponse.ok(productService.search(request));
    }

    @GetMapping("/mine")
    public ApiResponse<List<Map<String, Object>>> myProducts() {
        return ApiResponse.ok(productService.myProducts());
    }

    @GetMapping("/favorites/list")
    public ApiResponse<List<Map<String, Object>>> favorites() {
        return ApiResponse.ok(productService.myFavorites());
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        return ApiResponse.ok(productService.detail(id));
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> create(@Valid @RequestBody ProductCreateRequest request) {
        return ApiResponse.ok("商品提交成功，等待审核", productService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Map<String, Object>> update(@PathVariable Long id, @Valid @RequestBody ProductCreateRequest request) {
        return ApiResponse.ok("商品更新成功，等待重新审核", productService.update(id, request));
    }

    @PostMapping("/{id}/favorite")
    public ApiResponse<Map<String, Object>> favorite(@PathVariable Long id) {
        return ApiResponse.ok(productService.favorite(id, true));
    }

    @DeleteMapping("/{id}/favorite")
    public ApiResponse<Map<String, Object>> unfavorite(@PathVariable Long id) {
        return ApiResponse.ok(productService.favorite(id, false));
    }

    @GetMapping("/{id}/messages")
    public ApiResponse<List<Map<String, Object>>> messages(@PathVariable Long id) {
        return ApiResponse.ok(productService.listMessages(id));
    }

    @PostMapping("/{id}/messages")
    public ApiResponse<Map<String, Object>> message(@PathVariable Long id, @Valid @RequestBody MessageRequest request) {
        return ApiResponse.ok("留言成功", productService.leaveMessage(id, request));
    }

    @PostMapping("/{id}/reports")
    public ApiResponse<Map<String, Object>> report(@PathVariable Long id, @Valid @RequestBody ReportRequest request) {
        return ApiResponse.ok("举报成功", productService.report(id, request));
    }
}
