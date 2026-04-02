package com.shanudevcodes.fdpacb.features.users.data.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateNameRequest {
    @NotBlank(message = "Name is required")
    private String name;
}
