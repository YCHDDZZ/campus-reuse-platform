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
@RequestMapping("/api/donations")
public class DonationController {
    private final DonationService donationService;

    DonationController(DonationService donationService) {
        this.donationService = donationService;
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> create(@Valid @RequestBody DonationRequest request) {
        return ApiResponse.ok("捐赠申请已提交", donationService.create(request));
    }

    @GetMapping("/my")
    public ApiResponse<List<Map<String, Object>>> myDonations() {
        return ApiResponse.ok(donationService.myDonations());
    }
}
