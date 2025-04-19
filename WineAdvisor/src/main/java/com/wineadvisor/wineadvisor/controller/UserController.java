package com.wineadvisor.wineadvisor.controller;

import com.wineadvisor.wineadvisor.DTO.users.PasswordDTO;
import com.wineadvisor.wineadvisor.DTO.users.UpdateUserDTO;
import com.wineadvisor.wineadvisor.DTO.users.CreateUserDTO;
import com.wineadvisor.wineadvisor.exception.BadRequestException;
import com.wineadvisor.wineadvisor.service.UserService;
import com.wineadvisor.wineadvisor.model.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;

import lombok.RequiredArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    ////////////// POST //////////////
    @PostMapping
    public ResponseEntity<?> createUser(@NotNull(message = "New user info cannot be null.") @Valid @RequestBody CreateUserDTO createUserDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Location", "/api/user/" + createUserDTO.getUsername())
                .body(userService.createUser(createUserDTO));
    }

    ////////////// GET //////////////
    @GetMapping
    public ResponseEntity<?> getAllUsers(Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getAllUsers(pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<?> getUsersByName(
            @RequestParam(required = false, name = "firstName") String firstName,
            @RequestParam(required = false, name = "lastName") String lastName,
            Pageable pageable) {
        if (firstName == null && lastName == null) {
            throw new BadRequestException("firstName and lastName cannot be both null at the same time.");
        }
        if ((firstName != null && firstName.isBlank()) || (lastName != null && lastName.isBlank())) {
            throw new BadRequestException("Neither firstName nor lastName can be blank.");
        }

        Page<User> result = null;
        if (firstName != null) {
            if (lastName != null) {
                result = userService.getUsersByFullName(firstName, lastName, pageable);
            } else {
                result = userService.getUsersByFirstName(firstName, pageable);
            }
        } else {
            result = userService.getUsersByLastName(lastName, pageable);
        }

        // return ResponseEntity.status(HttpStatus.OK).header("documentCount", String.valueOf(result.size())).body(result);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/{username}")
    public ResponseEntity<?> getUserByUsername(
            @NotBlank(message = "Username cannot be blank.") @PathVariable String username) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getUserByUsername(username));
    }

    ////////////// PUT //////////////
    @PutMapping("/{username}")
    public ResponseEntity<?> updateUser(
            @NotBlank(message = "Username cannot be blank.") @PathVariable String username,
            @NotNull(message = "User update info cannot be null.") @Valid @RequestBody UpdateUserDTO updateUserDTO) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.updateUser(username, updateUserDTO));
    }

    @PutMapping("{username}/username/update")
    public ResponseEntity<?> updateUserUsername(
            @NotBlank(message = "Username cannot be blank.") @PathVariable(name = "username") String targetUsername,
            @NotBlank(message = "New username cannot be blank.") @Valid @Pattern(regexp = "^[a-zA-Z0-9_]{3,50}$", message = "The new username must be between 3 and 50 characters long and can contain letters, numbers, and underscores.") @RequestParam(required = true, name = "newUsername") String newUsername) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.updateUserUsername(targetUsername, newUsername));
    }

    @PutMapping("{username}/password/update")
    public ResponseEntity<?> updateUserPassword(
            @NotBlank(message = "Username cannot be blank.") @PathVariable String username,
            @NotNull(message = "Password update info cannot be null.") @Valid @RequestBody PasswordDTO passwordDTO) {
        if (passwordDTO.getOldPass() == null || passwordDTO.getOldPass().isBlank()) {
            throw new BadRequestException("Old password cannot blank.");
        }

        return ResponseEntity.status(HttpStatus.OK).body(userService.updateUserPassword(username, passwordDTO));
    }

    @PutMapping("/{username}/addLike")
    public ResponseEntity<?> addLike(
            @NotBlank(message = "Username cannot be blank.") @PathVariable String username,
            @NotNull(message = "reviewId cannot be null.") @RequestParam(name = "reviewId", required = true) Long reviewId) {
        if (reviewId <= 0) {
            throw new BadRequestException("reviewId must be greater than 0.");
        }

        return ResponseEntity.status(HttpStatus.OK).body(userService.addLike(username, reviewId));
    }

    @PutMapping("/{username}/removeLike")
    public ResponseEntity<?> removeLike(
            @NotBlank(message = "Username cannot be blank.") @PathVariable String username,
            @NotNull(message = "reviewId cannot be null.") @RequestParam(name = "reviewId", required = true) Long reviewId) {
        if (reviewId <= 0) {
            throw new BadRequestException("reviewId must be greater than 0.");
        }

        return ResponseEntity.status(HttpStatus.OK).body(userService.removeLike(username, reviewId));
    }

    @PutMapping("/{username}/addDislike")
    public ResponseEntity<?> addDislike(
            @NotBlank(message = "Username cannot be blank.") @PathVariable String username,
            @NotNull(message = "reviewId cannot be null.") @RequestParam(name = "reviewId", required = true) Long reviewId) {
        if (reviewId <= 0) {
            throw new BadRequestException("reviewId must be greater than 0.");
        }

        return ResponseEntity.status(HttpStatus.OK).body(userService.addDislike(username, reviewId));
    }

    @PutMapping("/{username}/removeDislike")
    public ResponseEntity<?> removeDislike(
            @NotBlank(message = "Username cannot be blank.") @PathVariable String username,
            @NotNull(message = "reviewId cannot be null.") @RequestParam(name = "reviewId", required = true) Long reviewId) {
        if (reviewId <= 0) {
            throw new BadRequestException("reviewId must be greater than 0.");
        }

        return ResponseEntity.status(HttpStatus.OK).body(userService.removeDislike(username, reviewId));
    }

    ////////////// DELETE //////////////
    @DeleteMapping("/{username}")
    public ResponseEntity<?> deleteUser(
            @NotBlank(message = "Username cannot be blank.") @PathVariable String username) {
        userService.deleteUser(username);
        return ResponseEntity.status(HttpStatus.OK).body("Utente eliminato correttamente.");
    }
}
