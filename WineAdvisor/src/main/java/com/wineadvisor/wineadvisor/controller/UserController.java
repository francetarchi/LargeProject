package com.wineadvisor.wineadvisor.controller;

import com.wineadvisor.wineadvisor.DTO.users.UpdateUserDTO;
import com.wineadvisor.wineadvisor.DTO.utils.PasswordDTO;
import com.wineadvisor.wineadvisor.DTO.users.CreateUserDTO;
import com.wineadvisor.wineadvisor.DTO.users.addFavoriteDTO;
import com.wineadvisor.wineadvisor.exception.BadRequestException;
import com.wineadvisor.wineadvisor.model.users.User;
import com.wineadvisor.wineadvisor.service.UserService;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;

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
            @Parameter(description = "First name", schema = @Schema(example = "Lorenzo")) @RequestParam(required = false) String firstName,
            @Parameter(description = "Last name", schema = @Schema(example = "Iacovelli")) @RequestParam(required = false) String lastName,
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
            @Parameter(description = "Username of the user to retrieve.", schema = @Schema(example = "yellowbutterfly631")) @NotBlank(message = "Username cannot be blank.") @PathVariable String username) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getUserByUsername(username));
    }

    @GetMapping("/{username}/suggestedFollows")
    @Secured({"ROLE_ADMIN", "ROLE_USER" })
    @PreAuthorize("#username == authentication.principal.username or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getUserSuggestedFollows(
            @Parameter(description = "Username of the user for whom to retrieve suggested follows.", schema = @Schema(example = "yellowbutterfly631")) @PathVariable String username) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getUserSuggestedFollows(username));
    }

    @GetMapping("/{username}/followers")
    public ResponseEntity<?> getUserFollowers(
            @Parameter(description = "Username of the user for whom to retrieve followers.", schema = @Schema(example = "yellowbutterfly631")) @PathVariable String username) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getUserFollowers(username));
    }

    @GetMapping("/{username}/followed")
    public ResponseEntity<?> getUserFollowed(
            @Parameter(description = "Username of the user for whom to retrieve following users.", schema = @Schema(example = "yellowbutterfly631")) @PathVariable String username) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getUserFollowed(username));
    }


    ////////////// PUT //////////////
    @PutMapping("/{username}")
    @Secured({ "ROLE_ADMIN", "ROLE_USER" })
    @PreAuthorize("#username == authentication.principal.username or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> updateUser(
            @Parameter(description = "Username of the user to update.", schema = @Schema(example = "yellowbutterfly631")) @NotBlank(message = "Username cannot be blank.") @PathVariable String username,
            @NotNull(message = "User update info cannot be null.") @Valid @RequestBody UpdateUserDTO updateUserDTO) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.updateUser(username, updateUserDTO));
    }

    @PutMapping("{username}/username/update")
    @Secured({ "ROLE_ADMIN", "ROLE_USER" })
    @PreAuthorize("#targetUsername == authentication.principal.username or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> updateUserUsername(
            @Parameter(description = "Username of the user to update.", schema = @Schema(example = "yellowbutterfly631")) @NotBlank(message = "Username cannot be blank.") @PathVariable(name = "username") String targetUsername,
            @NotBlank(message = "New username cannot be blank.") @Pattern(regexp = "^[a-zA-Z0-9_]{3,50}$", message = "The new username must be between 3 and 50 characters long and can contain letters, numbers, and underscores.") @RequestParam(required = true, name = "newUsername", defaultValue = "yellowbutterfly6311") String newUsername) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.updateUserUsername(targetUsername, newUsername));
    }

    @PutMapping("{username}/password/update")
    @PreAuthorize("hasRole('ROLE_USER') and #username == authentication.principal.username")
    public ResponseEntity<?> updateUserPassword(
            @Parameter(description = "Username of the user to update.", schema = @Schema(example = "yellowbutterfly631")) @NotBlank(message = "Username cannot be blank.") @PathVariable String username,
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
            @Parameter(description = "Username of the liker.", schema = @Schema(example = "yellowbutterfly631")) @NotBlank(message = "Username cannot be blank.") @PathVariable String username,
            @NotNull(message = "reviewId cannot be null.") @Positive(message = "reviewId must be a positive integer number.") @RequestParam(name = "reviewId", required = true, defaultValue = "1") Long reviewId) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.addLike(username, reviewId));
    }

    @PutMapping("/{username}/removeLike")
    @PreAuthorize("hasRole('ROLE_USER') and #username == authentication.principal.username")
    public ResponseEntity<?> removeLike(
            @Parameter(description = "Username of the unliker.", schema = @Schema(example = "yellowbutterfly631")) @NotBlank(message = "Username cannot be blank.") @PathVariable String username,
            @NotNull(message = "reviewId cannot be null.") @Positive(message = "reviewId must be a positive integer number.") @RequestParam(name = "reviewId", required = true, defaultValue = "1") Long reviewId) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.removeLike(username, reviewId));
    }

    @PutMapping("/{username}/addDislike")
    @PreAuthorize("hasRole('ROLE_USER') and #username == authentication.principal.username")
    public ResponseEntity<?> addDislike(
            @Parameter(description = "Username of the disliker.", schema = @Schema(example = "yellowbutterfly631")) @NotBlank(message = "Username cannot be blank.") @PathVariable String username,
            @NotNull(message = "reviewId cannot be null.") @Positive(message = "reviewId must be a positive integer number.") @RequestParam(name = "reviewId", required = true, defaultValue = "1") Long reviewId) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.addDislike(username, reviewId));
    }

    @PutMapping("/{username}/removeDislike")
    @PreAuthorize("hasRole('ROLE_USER') and #username == authentication.principal.username")
    public ResponseEntity<?> removeDislike(
            @Parameter(description = "Username of the undisliker.", schema = @Schema(example = "yellowbutterfly631")) @NotBlank(message = "Username cannot be blank.") @PathVariable String username,
            @NotNull(message = "reviewId cannot be null.") @Positive(message = "reviewId must be a positive integer number.") @RequestParam(name = "reviewId", required = true, defaultValue = "1") Long reviewId) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.removeDislike(username, reviewId));
    }

    ///// Operazioni su vini preferiti /////
    @PutMapping("/{username}/addFavorite")
    @PreAuthorize("hasRole('ROLE_USER') and #username == authentication.principal.username")
    public ResponseEntity<?> addFavorite(
            @Parameter(description = "Username of the user adding a favorite wine.", schema = @Schema(example = "yellowbutterfly631")) @NotBlank(message = "Username cannot be blank.") @PathVariable String username,
            @NotNull(message = "Wine info cannot be null.") @Valid @RequestBody addFavoriteDTO addFavoriteDTO) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.addFavorite(username, addFavoriteDTO));
    }

    @PutMapping("/{username}/removeFavorite")
    @PreAuthorize("hasRole('ROLE_USER') and #username == authentication.principal.username")
    public ResponseEntity<?> removeFavorite(
            @Parameter(description = "Username of the user removing a favorite wine.", schema = @Schema(example = "yellowbutterfly631")) @NotBlank(message = "Username cannot be blank.") @PathVariable String username,
            @NotNull(message = "Wine ID cannot be null.") @Positive(message = "Wine ID must be a positive integer number.") @RequestParam(name = "wineId", required = true, defaultValue = "1") Long wineId) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.removeFavorite(username, wineId));
    }

    ///// Operazioni su follow e unfollow /////
    @PutMapping("/{username}/follow")
    @Secured({ "ROLE_USER" })
    @PreAuthorize("#username == authentication.principal.username")
    public ResponseEntity<?> follow(
            @Parameter(description = "Username of the user who wants to follow another user.", schema = @Schema(example = "yellowbutterfly631")) @NotBlank(message = "Username cannot be blank.") @PathVariable String username,
            @NotBlank(message = "Target username cannot be blank.") @RequestParam(name = "targetUsername", required = true, defaultValue = "yellowduck514") String targetUsername) {
        userService.follow(username, targetUsername);
        return ResponseEntity.status(HttpStatus.OK).body(("Now following " + targetUsername));
    }

    @PutMapping("/{username}/unfollow")
    @Secured({ "ROLE_USER" })
    @PreAuthorize("#username == authentication.principal.username")
    public ResponseEntity<?> unfollow(
            @Parameter(description = "Username of the user who wants to unfollow another user.", schema = @Schema(example = "yellowbutterfly631")) @NotBlank(message = "Username cannot be blank.") @PathVariable String username,
            @NotBlank(message = "Target username cannot be blank.") @RequestParam(name = "targetUsername", required = true, defaultValue = "yellowduck514") String targetUsername) {
        userService.unfollow(username, targetUsername);
        return ResponseEntity.status(HttpStatus.OK).body(("Unfollowed " + targetUsername));
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
            @Parameter(description = "Username of the user to delete.", schema = @Schema(example = "yellowbutterfly631")) @NotBlank(message = "Username cannot be blank.") @PathVariable String username) {
        userService.deleteUser(username);
        return ResponseEntity.status(HttpStatus.OK).body("User \"" + username + "\" deleted successfully.");
    }
}
