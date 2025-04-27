package com.wineadvisor.wineadvisor.DTO.authentication;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticateUserDTO {
    @NotBlank(message = "Username is required")
    @Schema(description = "Username to authenticate", example = "user123")
    private String username;

    @NotBlank(message = "Password is required")
    @Schema(description = "Password to authenticate", example = "oldPass123!")
    private String password;
}