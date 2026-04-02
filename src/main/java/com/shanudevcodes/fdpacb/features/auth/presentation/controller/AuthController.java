package com.shanudevcodes.fdpacb.features.auth.presentation.controller;

import com.shanudevcodes.fdpacb.common.exception.dto.ApiResponse;
import com.shanudevcodes.fdpacb.features.auth.data.dto.*;
import com.shanudevcodes.fdpacb.features.auth.domain.service.AuthService;
import com.shanudevcodes.fdpacb.features.users.data.entity.UserModel;
import com.shanudevcodes.fdpacb.security.rbac.role.Role;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignUpResponse>> signup(
            @Valid @RequestBody SignupRequest request
    ) {
        SignUpResponse response = authService.createUser(request, Set.of(Role.VIEWER));
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<SignUpResponse>builder()
                        .status("Success")
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
                        .status("success")
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
                        .status("success")
                        .message("Refresh successful")
                        .data(tokens)
                        .build()
        );
    }

    @GetMapping("/me/capabilities")
    public ResponseEntity<ApiResponse<CapabilitiesResponse>> getCapabilities(
            @AuthenticationPrincipal UserModel user
    ) {
        return ResponseEntity.ok(ApiResponse.<CapabilitiesResponse>builder()
                .status("Success")
                .message("Capabilities fetched contextually via Authentication context")
                .data(authService.getCapabilities(user))
                .build());
    }
}
