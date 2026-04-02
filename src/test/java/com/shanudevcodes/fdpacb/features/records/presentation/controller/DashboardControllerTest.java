package com.shanudevcodes.fdpacb.features.records.presentation.controller;

import com.shanudevcodes.fdpacb.common.exception.dto.ApiResponse;
import com.shanudevcodes.fdpacb.features.records.data.dto.DashboardResponse;
import com.shanudevcodes.fdpacb.features.records.domain.service.RecordAnalyticsService;
import com.shanudevcodes.fdpacb.features.users.data.entity.UserModel;
import com.shanudevcodes.fdpacb.security.rbac.role.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock
    private RecordAnalyticsService analyticsService;

    @InjectMocks
    private DashboardController dashboardController;

    private UserModel mockUser;

    @BeforeEach
    void setUp() {
        mockUser = UserModel.builder()
                .id(UUID.randomUUID())
                .roles(Set.of(Role.ADMIN))
                .build();
    }

    @Test
    void getDashboard_ReturnsFormattedAnalytics() {
        BigDecimal amount = new BigDecimal("50000.0");
        when(analyticsService.getDashboard(any(), any()))
                .thenReturn(DashboardResponse.builder().netBalance(amount).build());

        ResponseEntity<ApiResponse<DashboardResponse>> response = 
                dashboardController.getDashboard(mockUser, List.of());
        
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("success", response.getBody().getStatus());
        assertEquals(amount, response.getBody().getData().getNetBalance());
    }
}
