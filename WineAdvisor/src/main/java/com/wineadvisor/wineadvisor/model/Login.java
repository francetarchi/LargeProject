package com.wineadvisor.wineadvisor.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class Login {
    @Schema(description = "Username", example = "user123")
    private String username;

    @Schema(description = "Password", example = "Pass123!")
    private String password;
}
