package com.wineadvisor.wineadvisor.DTO;

import com.wineadvisor.wineadvisor.model.User;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NewUserDTO {
    @NotBlank(message = "User info cannot be blank.")
    private User newUser;

    @NotBlank(message = "Password info cannot be blank.")
    private PasswordDTO passwordDTO;
}
