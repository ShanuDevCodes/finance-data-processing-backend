package com.shanudevcodes.fdpacb.features.users.presentation.controller;

import com.shanudevcodes.fdpacb.common.exception.dto.ApiResponse;
import com.shanudevcodes.fdpacb.features.users.data.dto.UpdateEmailRequest;
import com.shanudevcodes.fdpacb.features.users.data.dto.UpdateNameRequest;
import com.shanudevcodes.fdpacb.features.users.data.dto.UpdatePasswordRequest;
import com.shanudevcodes.fdpacb.features.users.data.dto.UpdateRolesRequest;
import com.shanudevcodes.fdpacb.features.users.data.entity.UserModel;
import com.shanudevcodes.fdpacb.features.users.domain.service.UserService;
import com.shanudevcodes.fdpacb.security.rbac.role.Role;
import com.shanudevcodes.fdpacb.security.rbac.role.Status;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.shanudevcodes.fdpacb.security.rbac.role.Role;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    public final UserService userService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserModel>>> getUsers(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) UUID analystId
    ) {
        return ResponseEntity.ok(
                ApiResponse.<Page<UserModel>>builder()
                        .status("success")
                        .message("Users fetched successfully")
                        .data(userService.getAllUsers(pageable, role, status, analystId))
                        .build()
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/roles")
    public ResponseEntity<ApiResponse<UserModel>> updateRoles(
            @PathVariable UUID id,
            @Valid @RequestBody List<UpdateRolesRequest> roles
    ) {
        return ResponseEntity.ok(
                ApiResponse.<UserModel>builder()
                        .status("success")
                        .message("Roles updated")
                        .data(userService.changeRole(id,roles.stream().map(UpdateRolesRequest::getRole).toList()))
                        .build()
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<UserModel>> toggleStatus(@PathVariable UUID id) {
        return ResponseEntity.ok(
                ApiResponse.<UserModel>builder()
                        .status("success")
                        .message("Status updated")
                        .data(userService.toggleStatus(id))
                        .build()
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{userId}/assign/{analystId}")
    public ResponseEntity<ApiResponse<UserModel>> assignUserToAnalyst(
            @PathVariable UUID userId,
            @PathVariable UUID analystId
    ) {
        return ResponseEntity.ok(
                ApiResponse.<UserModel>builder()
                        .status("success")
                        .message("User assigned to analyst")
                        .data(userService.assignViewerToAnalyst(userId,analystId))
                        .build()
        );
    }

    @PreAuthorize("hasAnyRole('VIEWER', 'ADMIN', 'ANALYST')")
    @PutMapping("/name")
    public ResponseEntity<ApiResponse<UserModel>> updateName(
            @AuthenticationPrincipal UserModel user,
            @Valid @RequestBody UpdateNameRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.<UserModel>builder()
                        .status("success")
                        .message("Name updated")
                        .data(userService.changeName(user, request.getName()))
                        .build()
        );
    }

    @PreAuthorize("hasAnyRole('VIEWER', 'ADMIN', 'ANALYST')")
    @PutMapping("/email")
    public ResponseEntity<ApiResponse<UserModel>> updateEmail(
            @AuthenticationPrincipal UserModel user,
            @Valid @RequestBody UpdateEmailRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.<UserModel>builder()
                        .status("success")
                        .message("Email updated")
                        .data(userService.changeEmail(user, request.getEmail()))
                        .build()
        );
    }

    @PreAuthorize("hasAnyRole('VIEWER', 'ADMIN', 'ANALYST')")
    @PutMapping("/password")
    public ResponseEntity<ApiResponse<UserModel>> updatePassword(
            @AuthenticationPrincipal UserModel user,
            @Valid @RequestBody UpdatePasswordRequest request
    ){
        return ResponseEntity.ok(
                ApiResponse.<UserModel>builder()
                        .status("success")
                        .message("Password updated")
                        .data(userService.changePassword(user, request.getPassword()))
                        .build()
        );
    }

    @PreAuthorize("hasRole('ANALYST')")
    @GetMapping("/assigned")
    public ResponseEntity<ApiResponse<List<UserModel>>> getAssignedViewers(
            @AuthenticationPrincipal UserModel user
    ) {
        return ResponseEntity.ok(
                ApiResponse.<List<UserModel>>builder()
                        .status("success")
                        .message("Assigned Viewers fetched")
                        .data(userService.getAssignedViewers(user.getId()))
                        .build()
        );
    }

}
