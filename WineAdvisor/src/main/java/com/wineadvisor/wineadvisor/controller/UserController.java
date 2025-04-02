package com.wineadvisor.wineadvisor.controller;

import com.wineadvisor.wineadvisor.config.UpdatePasswordRequest;
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
import jakarta.validation.Valid;

import java.net.URI;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    ////////////// POST //////////////
    @PostMapping
    public ResponseEntity<?> addUser(@Valid @RequestBody User newUser) {
        try {
            if (newUser == null) {
                throw new BadRequestException("User cannot be null.");
            }

            newUser.adjustRegistrationDate();
            
            return ResponseEntity.created(URI.create("/api/user/" + newUser.getLogin().getUsername())).body(userService.addUser(newUser));
        } catch (ConstraintViolationException e) {
            System.err.println("--- ERR: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
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
            return ResponseEntity.internalServerError().body("No users found.");
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> getUsersByName(
            @RequestParam(required = false, name = "firstName") String firstName,
            @RequestParam(required = false, name = "lastName") String lastName
        ) {
        try {
            if (firstName == null && lastName == null) {
                throw new BadRequestException("firstName and lastName cannot be both null.");
            }
            if ((firstName != null && firstName.isEmpty()) || (lastName != null && lastName.isEmpty())) {
                throw new BadRequestException("firstName and lastName cannot be empty.");
            }
            if (firstName != null && !firstName.matches("^[a-zA-ZÀ-ÿ'\\-\\s]+$")) {
                throw new BadRequestException("First name must contain only letters, spaces, hyphens, and apostrophes.");
            }
            if (lastName != null && !lastName.matches("^[a-zA-ZÀ-ÿ'\\-\\s]+$")) {
                throw new BadRequestException("First name must contain only letters, spaces, hyphens, and apostrophes.");
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
    public ResponseEntity<?> getUserByUsername(@PathVariable String username) {
        try {
            if (username == null || username.isEmpty()) {
                throw new BadRequestException("Username cannot be null or empty.");
            }
            if (!username.matches("^[a-zA-Z0-9_]{3,50}$")) {
                throw new BadRequestException("Username must be between 3 and 50 characters long and can contain letters, numbers, and underscores.");
            }

            return ResponseEntity.ok().body(userService.getUserByUsername(username));
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

    
    ////////////// PUT //////////////
    @PutMapping("/{username}")
    public ResponseEntity<?> updateUser(@PathVariable String username, @RequestBody User user) {
        try {
            if (username == null || username.isEmpty()) {
                throw new BadRequestException("Username cannot be null or empty.");
            }
            if (user == null) {
                throw new BadRequestException("User cannot be null.");
            }

            user.getLogin().setUsername(username);
            return ResponseEntity.ok().body(userService.updateUser(user));
        } catch (Exception e) {
            System.err.println("--- ERR: " + e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PutMapping("{username}/password/update")
    public ResponseEntity<?> updateUserPassword(@PathVariable String username, @RequestBody UpdatePasswordRequest updatePasswordRequest) {
        try {
            if (username == null || username.isEmpty()) {
                throw new BadRequestException("Username cannot be null or empty.");
            }
            if (updatePasswordRequest.getOldPass() == null || updatePasswordRequest.getOldPass().isEmpty()) {
                throw new BadRequestException("Old password cannot be null or empty.");
            }
            if (updatePasswordRequest.getNewPass() == null || updatePasswordRequest.getNewPass().isEmpty()) {
                throw new BadRequestException("New password cannot be null or empty.");
            }
            if (updatePasswordRequest.getConfirmPass() == null || updatePasswordRequest.getConfirmPass().isEmpty()) {
                throw new BadRequestException("Confirm password cannot be null or empty.");
            }

            return ResponseEntity.ok().body(userService.updateUserPassword(username, updatePasswordRequest.getOldPass(), updatePasswordRequest.getNewPass(), updatePasswordRequest.getConfirmPass()));
        } catch (Exception e) {
            System.err.println("--- ERR: " + e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }


    ////////////// DELETE //////////////
    @DeleteMapping
    public ResponseEntity<?> deleteUser(@PathVariable String username) {
        try {
            if (username == null || username.isEmpty()) {
                throw new BadRequestException("Username cannot be null or empty.");
            }

            userService.deleteUser(username);
            return ResponseEntity.ok().body("Utente eliminato correttamente.");
        } catch (Exception e) {
            System.err.println("--- ERR: " + e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
