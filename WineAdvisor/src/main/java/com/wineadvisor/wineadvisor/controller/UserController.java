package com.wineadvisor.wineadvisor.controller;

import com.wineadvisor.wineadvisor.DTO.users.UpdateUserDTO;
import com.wineadvisor.wineadvisor.DTO.utils.PasswordDTO;
import com.wineadvisor.wineadvisor.DTO.users.CreateUserDTO;
import com.wineadvisor.wineadvisor.DTO.users.addFavoriteDTO;
import com.wineadvisor.wineadvisor.exception.BadRequestException;
import com.wineadvisor.wineadvisor.model.users.User;
import com.wineadvisor.wineadvisor.service.UserService;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
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

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import lombok.RequiredArgsConstructor;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    ////////////////////////////////
    /////////// VARIABLES //////////
    ////////////////////////////////
    private final UserService userService;


    
    ////////////// POST //////////////
    @PostMapping
    public ResponseEntity<?> createUser(
            @NotNull(message = "New user info cannot be null.") @Valid @RequestBody CreateUserDTO createUserDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Location", "/api/users/" + createUserDTO.getUsername())
                .body(userService.createUser(createUserDTO));
    }


    ////////////// GET //////////////
    @GetMapping
    @Secured({ "ROLE_ADMIN" })
    public ResponseEntity<?> getAllUsers(
            @RequestParam(required = false, name = "page number", defaultValue = "0") @PositiveOrZero(message = "Page number must be positive or zero (or omitted).") Integer page) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getAllUsers(page));
    }

    @GetMapping("/search")
    public ResponseEntity<?> getUsersByName(
            @RequestParam(required = false, name = "firstName") String firstName,
            @RequestParam(required = false, name = "lastName") String lastName,
            @RequestParam(required = false, name = "page number", defaultValue = "0") @PositiveOrZero(message = "Page number must be positive or zero (or omitted).") Integer page) {
        if (firstName == null && lastName == null) {
            throw new BadRequestException("firstName and lastName cannot be both null at the same time.");
        }
        if ((firstName != null && firstName.isBlank()) || (lastName != null && lastName.isBlank())) {
            throw new BadRequestException("Neither firstName nor lastName can be blank.");
        }

        Page<User> result = null;
        if (firstName != null) {
            if (lastName != null) {
                result = userService.getUsersByFullName(firstName, lastName, page);
            } else {
                result = userService.getUsersByFirstName(firstName, page);
            }
        } else {
            result = userService.getUsersByLastName(lastName, page);
        }

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/{username}")
    public ResponseEntity<?> getUserByUsername(
            @NotBlank(message = "Username cannot be blank.") @PathVariable String username) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getUserByUsername(username));
    }

    @GetMapping("/neo4j/{username}")
    public ResponseEntity<Map<String, Object>> getUserFromGraph(@PathVariable String username) {
        Map<String, Object> result = userService.getUserFromGraph(username);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }


    ////////////// PUT //////////////
    @PutMapping("/{username}")
    @Secured({"ROLE_ADMIN", "ROLE_USER" })
    @PreAuthorize("#username == authentication.principal.username or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> updateUser(
            @NotBlank(message = "Username cannot be blank.") @PathVariable String username,
            @NotNull(message = "User update info cannot be null.") @Valid @RequestBody UpdateUserDTO updateUserDTO) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.updateUser(username, updateUserDTO));
    }

    @PutMapping("{username}/username/update")
    @Secured({"ROLE_ADMIN", "ROLE_USER" })
    @PreAuthorize("#targetUsername == authentication.principal.username or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> updateUserUsername(
            @NotBlank(message = "Username cannot be blank.") @PathVariable(name = "username") String targetUsername,
            @NotBlank(message = "New username cannot be blank.") @Pattern(regexp = "^[a-zA-Z0-9_]{3,50}$", message = "The new username must be between 3 and 50 characters long and can contain letters, numbers, and underscores.") @RequestParam(required = true, name = "newUsername") String newUsername) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.updateUserUsername(targetUsername, newUsername));
    }

    @PutMapping("{username}/password/update")
    @PreAuthorize("hasRole('ROLE_USER') and #username == authentication.principal.username")
    public ResponseEntity<?> updateUserPassword(
            @NotBlank(message = "Username cannot be blank.") @PathVariable String username,
            @NotNull(message = "Password update info cannot be null.") @Valid @RequestBody PasswordDTO passwordDTO) throws BadRequestException {
        if (passwordDTO.getOldPass() == null || passwordDTO.getOldPass().isBlank()) {
            throw new BadRequestException("Old password cannot blank.");
        }

        return ResponseEntity.status(HttpStatus.OK).body(userService.updateUserPassword(username, passwordDTO));
    }

    ///// Operazioni su like e dislike /////
    @PutMapping("/{username}/addLike")
    @PreAuthorize("hasRole('ROLE_USER') and #username == authentication.principal.username")
    public ResponseEntity<?> addLike(
            @NotBlank(message = "Username cannot be blank.") @PathVariable String username,
            @NotNull(message = "reviewId cannot be null.") @Positive(message = "reviewId must be a positive integer number.") @RequestParam(name = "reviewId", required = true) Long reviewId) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.addLike(username, reviewId));
    }

    @PutMapping("/{username}/removeLike")
    @PreAuthorize("hasRole('ROLE_USER') and #username == authentication.principal.username")
    public ResponseEntity<?> removeLike(
            @NotBlank(message = "Username cannot be blank.") @PathVariable String username,
            @NotNull(message = "reviewId cannot be null.") @Positive(message = "reviewId must be a positive integer number.") @RequestParam(name = "reviewId", required = true) Long reviewId) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.removeLike(username, reviewId));
    }

    @PutMapping("/{username}/addDislike")
    @PreAuthorize("hasRole('ROLE_USER') and #username == authentication.principal.username")
    public ResponseEntity<?> addDislike(
            @NotBlank(message = "Username cannot be blank.") @PathVariable String username,
            @NotNull(message = "reviewId cannot be null.") @Positive(message = "reviewId must be a positive integer number.") @RequestParam(name = "reviewId", required = true) Long reviewId) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.addDislike(username, reviewId));
    }

    @PutMapping("/{username}/removeDislike")
    @PreAuthorize("hasRole('ROLE_USER') and #username == authentication.principal.username")
    public ResponseEntity<?> removeDislike(
            @NotBlank(message = "Username cannot be blank.") @PathVariable String username,
            @NotNull(message = "reviewId cannot be null.") @Positive(message = "reviewId must be a positive integer number.") @RequestParam(name = "reviewId", required = true) Long reviewId) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.removeDislike(username, reviewId));
    }

    ///// Operazioni sui vini preferiti /////
    @PutMapping("/{username}/addFavorite")
    @PreAuthorize("hasRole('ROLE_USER') and #username == authentication.principal.username")
    public ResponseEntity<?> addFavorite(
            @NotBlank(message = "Username cannot be blank.") @PathVariable String username,
            @NotNull(message = "Wine info cannot be null.") @Valid @RequestBody addFavoriteDTO addFavoriteDTO) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.addFavorite(username, addFavoriteDTO));
    }

    @PutMapping("/{username}/removeFavorite")
    @PreAuthorize("hasRole('ROLE_USER') and #username == authentication.principal.username")
    public ResponseEntity<?> removeFavorite(
            @NotBlank(message = "Username cannot be blank.") @PathVariable String username,
            @NotNull(message = "wineId cannot be null.") @Positive(message = "wineId must be a positive integer number.") @RequestParam(name = "wineId", required = true) Long wineId) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.removeFavorite(username, wineId));
    }

    @PutMapping("/neo4j/{username}")
    public ResponseEntity<Void> updateUserInGraph(
            @PathVariable String username,
            @RequestBody Map<String, String> body) {
        userService.updateUserInGraph(
            username,
            body.get("firstName"),
            body.get("lastName"),
            body.get("thumbnail")
        );
        return ResponseEntity.ok().build();
    }

    ////////////// DELETE //////////////
    @DeleteMapping
    @Secured({ "ROLE_ADMIN" })
    public ResponseEntity<?> deleteAllUsers() {
        userService.deleteAllUsers();
        return ResponseEntity.status(HttpStatus.OK).body("All users deleted successfully.");
    }

    @DeleteMapping("/{username}")
    @Secured({ "ROLE_ADMIN", "ROLE_USER" })
    @PreAuthorize("#username == authentication.principal.username or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> deleteUser(
            @NotBlank(message = "Username cannot be blank.") @PathVariable String username) {
        userService.deleteUser(username);
        return ResponseEntity.status(HttpStatus.OK).body("User \"" + username + "\" deleted successfully.");
    }

    @DeleteMapping("/neo4j/{username}")
    public ResponseEntity<Void> deleteUserFromGraph(@PathVariable String username) {
        userService.deleteUserFromGraph(username);
        return ResponseEntity.noContent().build();
    }
}

