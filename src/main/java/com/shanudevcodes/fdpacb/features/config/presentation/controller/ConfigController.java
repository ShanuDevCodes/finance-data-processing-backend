package com.shanudevcodes.fdpacb.features.config.presentation.controller;

import com.shanudevcodes.fdpacb.common.exception.dto.ApiResponse;
import com.shanudevcodes.fdpacb.features.config.domain.service.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/config")
@RequiredArgsConstructor
public class ConfigController {
    
    private final ConfigService configService;
    
    @GetMapping("/enums")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEnums() {
        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .status("success")
                .message("Enums metadata fetched correctly")
                .data(configService.getMetadataEnums())
                .build());
    }
}
