package com.wineadvisor.wineadvisor.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wineadvisor.wineadvisor.config.UpdatePasswordRequest;
import com.wineadvisor.wineadvisor.model.User;
import com.wineadvisor.wineadvisor.service.UserService;

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

    @GetMapping("/username")
    public ResponseEntity<?> getUserByUsername(@RequestParam String username) {
        try {
            return ResponseEntity.ok().body(userService.getUserByUsername(username));
        } catch (Exception e) {
            System.err.println("--- ERR: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/firstname")
    public ResponseEntity<?> getUsersByFirstName(@RequestParam String firstName) {
        try {
            return ResponseEntity.ok().body(userService.getUsersByFirstName(firstName));
        } catch (Exception e) {
            System.err.println("--- ERR: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/lastname")
    public ResponseEntity<?> getUsersByLastName(@RequestParam String lastName) {
        try {
            return ResponseEntity.ok().body(userService.getUsersByLastName(lastName));
        } catch (Exception e) {
            System.err.println("--- ERR: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    
    ////////////// PUT //////////////
    @PutMapping
    public ResponseEntity<?> updateUser(@RequestBody User user) {
        try {
            return ResponseEntity.ok().body(userService.updateUser(user));
        } catch (Exception e) {
            System.err.println("--- ERR: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/password/update")
    public ResponseEntity<?> updateUserPassword(@RequestBody UpdatePasswordRequest body) {
        try {
            String username = body.getUsername();
            String oldPass = body.getOldPass();
            String newPass = body.getNewPass();
            String confirmPass = body.getConfirmPass();
            
            return ResponseEntity.ok().body(userService.updateUserPassword(username, oldPass, newPass, confirmPass));
        } catch (Exception e) {
            System.err.println("--- ERR: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    

    ////////////// DELETE //////////////
    @DeleteMapping
    public ResponseEntity<?> deleteUser(@RequestParam String username) {
        try {
            userService.deleteUser(username);
            return ResponseEntity.ok().body("Utente eliminato correttamente");
        } catch (Exception e) {
            System.err.println("--- ERR: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
