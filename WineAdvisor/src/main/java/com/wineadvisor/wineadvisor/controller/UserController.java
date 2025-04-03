package com.wineadvisor.wineadvisor.controller;

import com.wineadvisor.wineadvisor.config.UpdatePasswordRequest;
import com.wineadvisor.wineadvisor.exception.ResourceAlreadyExistsException;
import com.wineadvisor.wineadvisor.exception.ResourceNotFoundException;
import com.wineadvisor.wineadvisor.service.UserService;
import com.wineadvisor.wineadvisor.model.User;

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

import org.apache.coyote.BadRequestException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;

import java.net.URI;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    ////////////// POST //////////////
    @PostMapping
    public ResponseEntity<?> addUser(@NotNull(message = "User cannot be null.") @Valid @RequestBody User newUser) {
        try {
            newUser.adjustRegistrationDate();
            return ResponseEntity.created(URI.create("/api/user/" + newUser.getLogin().getUsername()))
                    .body(userService.addUser(newUser));
        } catch (ConstraintViolationException e) {
            System.err.println("--- ERR: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ResourceAlreadyExistsException e) {
            System.err.println("--- ERR: " + e.getMessage());
            return ResponseEntity.conflict().body(e.getMessage());
        } catch (BadRequestException e) {
            System.err.println("--- ERR: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            System.err.println("--- ERR: " + e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    ////////////// GET //////////////
    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        try {
            return ResponseEntity.ok().body(userService.getAllUsers());
        } catch (ResourceNotFoundException e) {
            System.err.println("--- ERR: " + e.getMessage());
            // return ResponseEntity.notFound().body(e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("--- ERR: " + e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> getUsersByName(
            @RequestParam(required = false, name = "firstName") String firstName,
            @RequestParam(required = false, name = "lastName") String lastName) {
        try {
            if (firstName == null && lastName == null) {
                throw new BadRequestException("firstName and lastName cannot be both null.");
            }
            if ((firstName != null && firstName.isBlank()) || (lastName != null && lastName.isBlank())) {
                throw new BadRequestException("firstName and lastName cannot be empty.");
            }

            if (firstName != null) {
                if (lastName != null) {
                    return ResponseEntity.ok().body(userService.getUsersByFullName(firstName, lastName));
                }

                return ResponseEntity.ok().body(userService.getUsersByFirstName(firstName));
            }

            return ResponseEntity.ok().body(userService.getUsersByLastName(lastName));
        } catch (ResourceNotFoundException e) {
            System.err.println("--- ERR: " + e.getMessage());
            // return ResponseEntity.notFound().body(e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (BadRequestException e) {
            System.err.println("--- ERR: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            System.err.println("--- ERR: " + e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @GetMapping("/{username}")
    public ResponseEntity<?> getUserByUsername(
            @NotBlank(message = "Username cannot be null or empty.") @PathVariable String username) {
        try {
            return ResponseEntity.ok().body(userService.getUserByUsername(username));
        } catch (ConstraintViolationException e) {
            System.err.println("--- ERR: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            System.err.println("--- ERR: " + e.getMessage());
            // return ResponseEntity.notFound().body(e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("--- ERR: " + e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    
    ////////////// PUT //////////////
    @PutMapping("/{username}")
    public ResponseEntity<?> updateUser(
            @NotBlank(message = "Username cannot be null or empty.") @PathVariable String username,
            @NotNull(message = "User cannot be null.") @Valid @RequestBody User user) {
        try {
            user.getLogin().setUsername(username);
            return ResponseEntity.ok().body(userService.updateUser(user));
        } catch (ConstraintViolationException e) {
            System.err.println("--- ERR: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            System.err.println("--- ERR: " + e.getMessage());
            // return ResponseEntity.notFound().body(e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (ResourceAlreadyExistsException e) {
            System.err.println("--- ERR: " + e.getMessage());
            return ResponseEntity.conflict().body(e.getMessage());
        } catch (Exception e) {
            System.err.println("--- ERR: " + e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PutMapping("{username}/password/update")
    public ResponseEntity<?> updateUserPassword(
            @NotBlank(message = "Username cannot be null or empty.") @PathVariable String username,
            @NotNull(message = "Password update info cannot be null.") @Valid @RequestBody UpdatePasswordRequest updatePasswordRequest) {
        try {
            return ResponseEntity.ok().body(userService.updateUserPassword(username, updatePasswordRequest.getOldPass(), updatePasswordRequest.getNewPass(), updatePasswordRequest.getConfirmPass()));
        } catch (ConstraintViolationException e) {
            System.err.println("--- ERR: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("--- ERR: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            System.err.println("--- ERR: " + e.getMessage());
            // return ResponseEntity.notFound().body(e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("--- ERR: " + e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }


    ////////////// DELETE //////////////
    @DeleteMapping
    public ResponseEntity<?> deleteUser(
            @NotBlank(message = "Username cannot be null or empty.") @PathVariable String username) {
        try {
            userService.deleteUser(username);
            return ResponseEntity.ok().body("Utente eliminato correttamente.");
        } catch (ConstraintViolationException e) {
            System.err.println("--- ERR: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            System.err.println("--- ERR: " + e.getMessage());
            // return ResponseEntity.notFound().body(e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("--- ERR: " + e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
