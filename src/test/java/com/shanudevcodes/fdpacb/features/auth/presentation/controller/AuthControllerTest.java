package com.shanudevcodes.fdpacb.features.auth.presentation.controller;

import com.shanudevcodes.fdpacb.common.exception.dto.ApiResponse;
import com.shanudevcodes.fdpacb.features.auth.data.dto.*;
import com.shanudevcodes.fdpacb.features.auth.domain.service.AuthService;
import com.shanudevcodes.fdpacb.features.users.data.entity.UserModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private UserModel mockUser;

    @BeforeEach
    void setUp() {
        mockUser = UserModel.builder().id(UUID.randomUUID()).build();
    }

    @Test
    void signup_ReturnsCreated() {
        SignupRequest dto = new SignupRequest();

        ResponseEntity<?> response = authController.signup(dto);

        assertEquals(201, response.getStatusCode().value());
        verify(authService).createUser(any(), any());
    }

    @Test
    void login_ReturnsOk() {
        LoginRequest req = new LoginRequest();
        when(authService.loginUser(any())).thenReturn(new TokenPair("access", "refresh"));

        ResponseEntity<?> response = authController.login(req);

        assertEquals(200, response.getStatusCode().value());
        verify(authService).loginUser(req);
    }

    @Test
    void refresh_ReturnsOk() {
        RefreshRequest req = new RefreshRequest();
        req.setRefreshToken("fakeToken");

        ResponseEntity<?> response = authController.refresh(req);

        assertEquals(200, response.getStatusCode().value());
        verify(authService).refresh("fakeToken");
    }

    @Test
    void getCapabilities_ReturnsOk() {
        when(authService.getCapabilities(any())).thenReturn(CapabilitiesResponse.builder().build());

        ResponseEntity<ApiResponse<CapabilitiesResponse>> response = authController.getCapabilities(mockUser);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        verify(authService).getCapabilities(mockUser);
    }
}
