package com.shanudevcodes.fdpacb.features.users.data.dto;

import com.shanudevcodes.fdpacb.common.exception.validation.anotation.ValidEnum;
import com.shanudevcodes.fdpacb.security.rbac.role.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateRolesRequest {
    @ValidEnum(enumClass = Role.class, message = "Invalid role")
    private String role;
}
