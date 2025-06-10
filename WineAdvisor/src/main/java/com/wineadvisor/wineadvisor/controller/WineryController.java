package com.wineadvisor.wineadvisor.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;

import com.wineadvisor.wineadvisor.DTO.utils.PasswordDTO;
import com.wineadvisor.wineadvisor.DTO.wineries.CreateWineryDTO;
import com.wineadvisor.wineadvisor.DTO.wineries.UpdateWineryDTO;
import com.wineadvisor.wineadvisor.exception.BadRequestException;
import com.wineadvisor.wineadvisor.service.WineryService;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/wineries")
@RequiredArgsConstructor
public class WineryController {
    ////////////////////////////////
    /////////// VARIABLES //////////
    ////////////////////////////////
    private final WineryService wineryService;
    
    
    
    ///////////// POST /////////////
    @PostMapping
    public ResponseEntity<?> createWinery(
            @NotNull(message = "New winery info cannot be null.") @Valid @RequestBody CreateWineryDTO createWineryDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Location", "/api/user/" + createWineryDTO.getUsername())
                .body(wineryService.createWinery(createWineryDTO));
    }
    

    ////////////// GET /////////////
    @GetMapping
    @Secured({ "ROLE_ADMIN" })
    public ResponseEntity<?> getAllWineries(
            @RequestParam(required = false, name = "page number", defaultValue = "0") @PositiveOrZero Integer page) {
        return ResponseEntity.status(HttpStatus.OK).body(wineryService.getAllWineries(page));
    }

    @GetMapping("/search")
    public ResponseEntity<?> getWineriesByName(
            @NotBlank(message = "Name cannot be blank.") @RequestParam(required = true, name = "name", defaultValue = "ARPEPE") String name,
            @RequestParam(required = false, name = "page number", defaultValue = "0") @PositiveOrZero(message = "Page number must be positive or zero (or omitted).") Integer page) {
        return ResponseEntity.status(HttpStatus.OK).body(wineryService.getWineriesByName(name, page));
    }

    @GetMapping("/{username}")
    public ResponseEntity<?> getWineryByUsername(
            @Parameter(description = "Username of the winery to retrieve.", schema = @Schema(example = "arpepe3749")) @NotBlank(message = "Username cannot be blank.") @PathVariable String username) {
        return ResponseEntity.status(HttpStatus.OK).body(wineryService.getWineryByUsername(username));
    }


    ////////////// PUT /////////////
    @PutMapping("/{username}")
    @Secured({"ROLE_ADMIN", "ROLE_WINERY" })
    @PreAuthorize("#username == authentication.principal.username or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> updateWinery(
            @Parameter(description = "Username of the winery to update.", schema = @Schema(example = "arpepe3749")) @NotBlank(message = "Username cannot be blank.") @PathVariable String username,
            @NotNull(message = "Winery update info cannot be null.") @Valid @RequestBody UpdateWineryDTO UpdateWineryDTO) {
        return ResponseEntity.status(HttpStatus.OK).body(wineryService.updateWinery(username, UpdateWineryDTO));
    }

    @PutMapping("{username}/username/update")
    @Secured({"ROLE_ADMIN", "ROLE_WINERY" })
    @PreAuthorize("#username == authentication.principal.username or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> updateWineryUsername(
            @Parameter(description = "Username of the winery to update.", schema = @Schema(example = "arpepe3749")) @NotBlank(message = "Username cannot be blank.") @PathVariable String username,
            @NotBlank(message = "New username cannot be blank.") @RequestParam(required = true, defaultValue = "arpepe3750") String newUsername) {
        return ResponseEntity.status(HttpStatus.OK).body(wineryService.updateWineryUsername(username, newUsername));
    }

    @PutMapping("{username}/password/update")
    @PreAuthorize("hasRole('ROLE_WINERY') and #username == authentication.principal.username")
    public ResponseEntity<?> updateWineryPassword(
            @Parameter(description = "Username of the winery to update.", schema = @Schema(example = "arpepe3749")) @NotBlank(message = "Username cannot be blank.") @PathVariable String username,
            @NotNull(message = "Password update info cannot be null.") @Valid @RequestBody PasswordDTO passwordDTO) throws BadRequestException {
        if (passwordDTO.getOldPass() == null || passwordDTO.getOldPass().isBlank()) {
            throw new BadRequestException("Old password cannot null or blank.");
        }

        return ResponseEntity.status(HttpStatus.OK).body(wineryService.updateWineryPassword(username, passwordDTO));
    }

    @PutMapping("/{username}/addImage")
    @Secured({"ROLE_ADMIN", "ROLE_WINERY" })
    @PreAuthorize("#username == authentication.principal.username or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> addImage(
            @Parameter(description = "Username of the winery to update.", schema = @Schema(example = "arpepe3749")) @NotBlank(message = "Username cannot be blank.") @PathVariable String username,
            @NotBlank(message = "Image link cannot be blank.") @RequestParam(name = "image", required = true, defaultValue = "https://i.postimg.cc/50qR82Y4/winery174.jpg") String image) {
        return ResponseEntity.status(HttpStatus.OK).body(wineryService.addImage(username, image));
    }

    @PutMapping("/{username}/removeImage")
    @Secured({"ROLE_ADMIN", "ROLE_WINERY" })
    @PreAuthorize("#username == authentication.principal.username or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> removeImage(
            @Parameter(description = "Username of the winery to update.", schema = @Schema(example = "arpepe3749")) @NotBlank(message = "Username cannot be blank.") @PathVariable String username,
            @NotBlank(message = "Image link cannot be blank.") @RequestParam(name = "image", required = true, defaultValue = "https://i.postimg.cc/50qR82Y4/winery174.jpg") String image) {
        return ResponseEntity.status(HttpStatus.OK).body(wineryService.removeImage(username, image));
    }

    
    //////////// DELETE ////////////
    @DeleteMapping
    @Secured({ "ROLE_ADMIN" })
    public ResponseEntity<?> deleteAllWineries() {
        wineryService.deleteAllWineries();
        return ResponseEntity.status(HttpStatus.OK).body("All wineries deleted successfully.");
    }

    @DeleteMapping("/{username}")
    @Secured({"ROLE_ADMIN", "ROLE_WINERY" })
    @PreAuthorize("#username == authentication.principal.username or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> deleteWinery(
            @Parameter(description = "Username of the winery to delete.", schema = @Schema(example = "arpepe3749")) @NotBlank(message = "Username cannot be blank.") @PathVariable String username) {
        wineryService.deleteWinery(username);
        return ResponseEntity.status(HttpStatus.OK).body("Winery \"" + username + "\" deleted successfully.");
    }
}
