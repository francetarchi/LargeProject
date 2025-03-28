package com.wineadvisor.wineadvisor.controller;

import java.net.URI;

import org.apache.coyote.BadRequestException;
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

import com.wineadvisor.wineadvisor.config.UpdatePasswordRequest;
import com.wineadvisor.wineadvisor.service.UserService;
import com.wineadvisor.wineadvisor.model.User;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    ////////////// POST //////////////
    @PostMapping
    public ResponseEntity<?> addUser(@RequestBody User newUser) {
        try {
            if (newUser == null) {
                throw new BadRequestException("User cannot be null");
            }
            
            return ResponseEntity.created(URI.create("/api/user/" + newUser.getLogin().getUsername())).body(userService.addUser(newUser));
        } catch (Exception e) {
            System.err.println("--- ERR: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    ////////////// GET //////////////
    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        try {
            return ResponseEntity.ok().body(userService.getAllUsers());
        } catch (Exception e) {
            System.err.println("--- ERR: " + e.getMessage());
            return ResponseEntity.badRequest().body("No users found");
        }
    }

    @GetMapping
    public ResponseEntity<?> getUsersByFirstName(@RequestParam String firstName) {
        try {
            return ResponseEntity.ok().body(userService.getUsersByFirstName(firstName));
        } catch (Exception e) {
            System.err.println("--- ERR: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getUsersByLastName(@RequestParam String lastName) {
        try {
            return ResponseEntity.ok().body(userService.getUsersByLastName(lastName));
        } catch (Exception e) {
            System.err.println("--- ERR: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{username}")
    public ResponseEntity<?> getUserByUsername(@PathVariable String username) {
        try {
            if (username == null || username.isEmpty()) {
                throw new BadRequestException("Username cannot be null or empty");
            }

            return ResponseEntity.ok().body(userService.getUserByUsername(username));
        } catch (Exception e) {
            System.err.println("--- ERR: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    
    ////////////// PUT //////////////
    @PutMapping("/{username}")
    public ResponseEntity<?> updateUser(@PathVariable String username, @RequestBody User user) {
        try {
            if (username == null || username.isEmpty()) {
                throw new BadRequestException("Username cannot be null or empty");
            }
            if (user == null) {
                throw new BadRequestException("User cannot be null");
            }

            user.getLogin().setUsername(username);
            return ResponseEntity.ok().body(userService.updateUser(user));
        } catch (Exception e) {
            System.err.println("--- ERR: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("{username}/password/update")
    public ResponseEntity<?> updateUserPassword(@PathVariable String username, @RequestBody UpdatePasswordRequest updatePasswordRequest) {
        try {
            if (username == null || username.isEmpty()) {
                throw new BadRequestException("Username cannot be null or empty");
            }
            if (updatePasswordRequest.getOldPass() == null || updatePasswordRequest.getOldPass().isEmpty()) {
                throw new BadRequestException("Old password cannot be null or empty");
            }
            if (updatePasswordRequest.getNewPass() == null || updatePasswordRequest.getNewPass().isEmpty()) {
                throw new BadRequestException("New password cannot be null or empty");
            }
            if (updatePasswordRequest.getConfirmPass() == null || updatePasswordRequest.getConfirmPass().isEmpty()) {
                throw new BadRequestException("Confirm password cannot be null or empty");
            }

            return ResponseEntity.ok().body(userService.updateUserPassword(username, updatePasswordRequest.getOldPass(), updatePasswordRequest.getNewPass(), updatePasswordRequest.getConfirmPass()));
        } catch (Exception e) {
            System.err.println("--- ERR: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    ////////////// DELETE //////////////
    @DeleteMapping
    public ResponseEntity<?> deleteUser(@PathVariable String username) {
        try {
            if (username == null || username.isEmpty()) {
                throw new BadRequestException("Username cannot be null or empty");
            }

            userService.deleteUser(username);
            return ResponseEntity.ok().body("Utente eliminato correttamente");
        } catch (Exception e) {
            System.err.println("--- ERR: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
