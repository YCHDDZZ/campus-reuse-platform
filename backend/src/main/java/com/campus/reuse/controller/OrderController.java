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
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> create(@Valid @RequestBody OrderCreateRequest request) {
        return ApiResponse.ok("下单成功", orderService.create(request));
    }

    @GetMapping("/my")
    public ApiResponse<List<Map<String, Object>>> myOrders() {
        return ApiResponse.ok(orderService.myOrders());
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<Map<String, Object>> cancel(@PathVariable Long id) {
        return ApiResponse.ok("订单已取消", orderService.cancel(id));
    }

    @PostMapping("/{id}/complete")
    public ApiResponse<Map<String, Object>> complete(@PathVariable Long id) {
        return ApiResponse.ok("订单已完成", orderService.complete(id));
    }

    @PostMapping("/{id}/dispute")
    public ApiResponse<Map<String, Object>> dispute(@PathVariable Long id, @Valid @RequestBody DisputeRequest request) {
        return ApiResponse.ok("纠纷已提交", orderService.dispute(id, request));
    }

    @PostMapping("/{id}/review")
    public ApiResponse<Map<String, Object>> review(@PathVariable Long id, @Valid @RequestBody ReviewRequest request) {
        return ApiResponse.ok("评价成功", orderService.review(id, request));
    }
}
