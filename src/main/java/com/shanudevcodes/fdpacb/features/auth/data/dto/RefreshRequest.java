package com.shanudevcodes.fdpacb.features.auth.data.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshRequest {
    @NotBlank(message = "Refresh Token is required")
    private String refreshToken;
}
