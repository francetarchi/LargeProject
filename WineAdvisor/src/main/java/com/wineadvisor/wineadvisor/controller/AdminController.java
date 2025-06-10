package com.wineadvisor.wineadvisor.controller;

import com.wineadvisor.wineadvisor.DTO.utils.PasswordDTO;
import com.wineadvisor.wineadvisor.DTO.admins.CreateAdminDTO;
import com.wineadvisor.wineadvisor.DTO.admins.UpdateAdminDTO;
import com.wineadvisor.wineadvisor.service.AdminService;
import com.wineadvisor.wineadvisor.exception.BadRequestException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/admins")
@RequiredArgsConstructor
public class AdminController {
    ////////////////////////////////
    /////////// VARIABLES //////////
    ////////////////////////////////
    private final AdminService adminService;



    ////////////// POST //////////////
    @PostMapping
    public ResponseEntity<?> createAdmin(
        @NotNull(message = "New admin info cannot be null.") @Valid @RequestBody CreateAdminDTO createAdminDTO) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .header("Location", "/api/admins/" + createAdminDTO.getUsername())
                    .body(adminService.createAdmin(createAdminDTO));
    }
    

    ////////////// GET //////////////
    @GetMapping
    public ResponseEntity<?> getAllAdmins(
            @RequestParam(required = false, name = "page number", defaultValue = "0") @PositiveOrZero Integer page) {
        return ResponseEntity.status(HttpStatus.OK).body(adminService.getAllAdmins(page));
    }

    @GetMapping("/{username}")
    public ResponseEntity<?> getAdminByUsername(
            @Parameter(description = "Username of the admin to retrieve.", schema = @Schema(example = "admin")) @NotBlank(message = "Username cannot be blank.") @PathVariable String username) {
        return ResponseEntity.status(HttpStatus.OK).body(adminService.getAdminByUsername(username));
    }


    ////////////// PUT //////////////
    @PutMapping("/{username}")
    @PreAuthorize("#username == authentication.principal.username")
    public ResponseEntity<?> updateAdmin(
            @Parameter(description = "Username of the admin to update.", schema = @Schema(example = "admin")) @NotBlank(message = "Username cannot be blank.") @PathVariable String username,
            @NotNull(message = "Admin update info cannot be null.") @Valid @RequestBody UpdateAdminDTO updateAdminDTO) {
        return ResponseEntity.status(HttpStatus.OK).body(adminService.updateAdmin(username, updateAdminDTO));
    }

    @PutMapping("{username}/username/update")
    @PreAuthorize("#targetUsername == authentication.principal.username")
    public ResponseEntity<?> updateAdminUsername(
            @Parameter(description = "Username of the admin to update.", schema = @Schema(example = "admin")) @NotBlank(message = "Username cannot be blank.") @PathVariable(name = "username") String targetUsername,
            @NotBlank(message = "New username cannot be blank.") @Pattern(regexp = "^[a-zA-Z0-9_]{3,50}$", message = "The new username must be between 3 and 50 characters long and can contain letters, numbers, and underscores.") @RequestParam(required = true, name = "newUsername", defaultValue = "admin123") String newUsername) {
        return ResponseEntity.status(HttpStatus.OK).body(adminService.updateAdminUsername(targetUsername, newUsername));
    }

    @PutMapping("{username}/password/update")
    @PreAuthorize("#username == authentication.principal.username")
    public ResponseEntity<?> updateAdminPassword(
            @Parameter(description = "Username of the admin to update.", schema = @Schema(example = "admin")) @NotBlank(message = "Username cannot be blank.") @PathVariable String username,
            @NotNull(message = "Password update info cannot be null.") @Valid @RequestBody PasswordDTO passwordDTO) throws BadRequestException {
        if (passwordDTO.getOldPass() == null || passwordDTO.getOldPass().isBlank()) {
            throw new BadRequestException("Old password cannot blank.");
        }

        return ResponseEntity.status(HttpStatus.OK).body(adminService.updateAdminPassword(username, passwordDTO));
    }
    

    ////////////// DELETE //////////////
    @DeleteMapping("/{username}")
    @PreAuthorize("#username == authentication.principal.username")
    public ResponseEntity<?> deleteAdmin(
            @Parameter(description = "Username of the admin to delete.", schema = @Schema(example = "admin")) @NotBlank(message = "Username cannot be blank.") @PathVariable String username) {
        adminService.deleteAdmin(username);
        return ResponseEntity.status(HttpStatus.OK).body("Admin \"" + username + "\" deleted successfully.");
    }
}
