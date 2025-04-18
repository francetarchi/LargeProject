package com.wineadvisor.wineadvisor.DTO.UserDTO;

import com.wineadvisor.wineadvisor.model.User;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NewUserDTO {
    @NotNull(message = "User info cannot be blank.")
    @Valid
    private User newUser;

    @NotNull(message = "Password info cannot be blank.")
    @Valid
    private PasswordDTO passwordDTO;
}
