package com.shanudevcodes.fdpacb.features.records.presentation.controller;

import com.shanudevcodes.fdpacb.common.exception.dto.ApiResponse;
import com.shanudevcodes.fdpacb.features.records.data.dto.DashboardResponse;
import com.shanudevcodes.fdpacb.features.records.domain.service.RecordAnalyticsService;
import com.shanudevcodes.fdpacb.features.users.data.entity.UserModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {
    private final RecordAnalyticsService analyticsService;

    public DashboardController(RecordAnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST', 'VIEWER')")
    @GetMapping
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard(
            @AuthenticationPrincipal UserModel user
    ) {
        DashboardResponse response = analyticsService.getDashboard(user);
        return ResponseEntity.ok(
                ApiResponse.<DashboardResponse>builder()
                        .status("success")
                        .message("Dashboard fetched successfully")
                        .data(response)
                        .build()
        );
    }
}
