package com.shanudevcodes.fdpacb.features.config.presentation.controller;

import com.shanudevcodes.fdpacb.common.exception.dto.ApiResponse;
import com.shanudevcodes.fdpacb.features.config.domain.service.ConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfigControllerTest {

    @Mock
    private ConfigService configService;

    @InjectMocks
    private ConfigController configController;

    @BeforeEach
    void setUp() {
    }

    @Test
    void getEnums_ReturnsValidEnums() {
        when(configService.getMetadataEnums()).thenReturn(Map.of(
                "roles", new Object(),
                "recordTypes", new Object(),
                "recordCategories", new Object(),
                "userStatuses", new Object()
        ));
        
        ResponseEntity<ApiResponse<Map<String, Object>>> response = configController.getEnums();
        
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("success", response.getBody().getStatus());
        Map<String, Object> data = response.getBody().getData();
        assertNotNull(data.get("roles"));
        assertNotNull(data.get("recordTypes"));
        assertNotNull(data.get("recordCategories"));
        assertNotNull(data.get("userStatuses"));
    }
}
