package com.wineadvisor.wineadvisor.config;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdatePasswordRequest {
    @NotBlank(message = "Old password is required")
    @Schema(description = "Old password", example = "oldPass123!")
    private String oldPass;

    @NotBlank(message = "New password is required")
    @Schema(description = "New password", example = "newPass123!")
    private String newPass;

    @NotBlank(message = "Confirm password is required")
    @Schema(description = "Confirm new password", example = "newPass123!")
    private String confirmPass;
}
