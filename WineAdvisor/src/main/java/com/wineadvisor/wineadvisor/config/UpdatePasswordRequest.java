package com.wineadvisor.wineadvisor.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UpdatePasswordRequest {
    @Schema(description = "Username", example = "user123")
    private String username;

    @Schema(description = "Old password", example = "oldPass123!")
    private String oldPass;

    @Schema(description = "New password", example = "newPass123!")
    private String newPass;

    @Schema(description = "Confirm new password", example = "newPass123!")
    private String confirmPass;
}
