package com.shanudevcodes.fdpacb.features.auth.presentation.controller;

import com.shanudevcodes.fdpacb.common.exception.dto.ApiResponse;
import com.shanudevcodes.fdpacb.features.auth.data.dto.*;
import com.shanudevcodes.fdpacb.features.auth.domain.service.AuthService;
import com.shanudevcodes.fdpacb.security.rbac.role.Role;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignUpResponse>> signup(
            @Valid @RequestBody SignupRequest request
    ) {
        SignUpResponse response = authService.createUser(request, Set.of(Role.VIEWER));
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<SignUpResponse>builder()
                        .status("success")
                        .message("User created successfully")
                        .data(response)
                        .build()
        );
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        TokenPair tokens = authService.loginUser(request);
        return ResponseEntity.ok(
                ApiResponse.<LoginResponse>builder()
                        .status("Success")
                        .message("Login successful")
                        .data(new LoginResponse(
                                tokens.getAccessToken(),
                                tokens.getRefreshToken()
                        ))
                        .build()
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenPair>> refresh(
            @Valid @RequestBody RefreshRequest request
    ) {
        String refreshToken = request.getRefreshToken();
        TokenPair tokens = authService.refresh(refreshToken);
        return ResponseEntity.ok(
                ApiResponse.<TokenPair>builder()
                        .status("Success")
                        .message("Refresh successful")
                        .data(tokens)
                        .build()
        );
    }

}
