package com.shanudevcodes.fdpacb.features.users.data.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePasswordRequest {
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 64, message = "Password must be of minimum 8 characters")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,64}$",
            message = "Password must include uppercase, lowercase, number, and special character"
    )
    private String password;
}
