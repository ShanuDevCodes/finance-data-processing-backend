package com.shanudevcodes.fdpacb.features.users.presentation.controller;

import com.shanudevcodes.fdpacb.common.exception.dto.ApiResponse;
import com.shanudevcodes.fdpacb.features.users.data.dto.UpdateRolesRequest;
import com.shanudevcodes.fdpacb.features.users.data.entity.UserModel;
import com.shanudevcodes.fdpacb.features.users.domain.service.UserService;
import com.shanudevcodes.fdpacb.security.rbac.role.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private UserModel testUser;

    @BeforeEach
    void setUp() {
        testUser = UserModel.builder().id(UUID.randomUUID()).build();
    }

    @Test
    void getUsers_ReturnsOk() {
        when(userService.getAllUsers(any(), eq(null), eq(null), eq(null))).thenReturn(Page.empty());

        ResponseEntity<ApiResponse<Page<UserModel>>> response = userController.getUsers(Pageable.unpaged(), null, null, null);
        
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    @Test
    void toggleStatus_ReturnsOk() {
        UUID userId = UUID.randomUUID();

        ResponseEntity<?> response = userController.toggleStatus(userId);
        
        assertEquals(200, response.getStatusCode().value());
        verify(userService).toggleStatus(userId);
    }

    @Test
    void updateRoles_ReturnsOk() {
        UUID userId = UUID.randomUUID();
        UpdateRolesRequest roleReq = new UpdateRolesRequest();
        roleReq.setRole(Role.ANALYST.name());

        ResponseEntity<?> response = userController.updateRoles(userId, List.of(roleReq));

        assertEquals(200, response.getStatusCode().value());
        verify(userService).changeRole(userId, List.of(Role.ANALYST.name()));
    }

    @Test
    void assignUserToAnalyst_ReturnsOk() {
        UUID analystId = UUID.randomUUID();
        UUID viewerId = UUID.randomUUID();

        ResponseEntity<?> response = userController.assignUserToAnalyst(viewerId, analystId);

        assertEquals(200, response.getStatusCode().value());
        verify(userService).assignViewerToAnalyst(viewerId, analystId);
    }

    @Test
    void getAssignedViewers_ReturnsOk() {
        when(userService.getAssignedViewers(any())).thenReturn(List.of());

        ResponseEntity<?> response = userController.getAssignedViewers(testUser);

        assertEquals(200, response.getStatusCode().value());
        verify(userService).getAssignedViewers(testUser.getId());
    }
}
