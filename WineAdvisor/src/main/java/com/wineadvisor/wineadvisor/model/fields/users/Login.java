package com.wineadvisor.wineadvisor.model.fields.users;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class Login {
    @NotBlank(message = "Username cannot be blank.")
    @Pattern(regexp = "^[a-zA-Z0-9_]{3,50}$", message = "Username must be between 3 and 50 characters long and can contain letters, numbers, and underscores.")
    @Schema(description = "Username", example = "user123")
    private String username;

    @Schema(description = "Password", example = "Pass123!")
    private String password;
}
