package com.wineadvisor.wineadvisor.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wineadvisor.wineadvisor.DTO.authentication.AuthenticateUserDTO;

import jakarta.validation.constraints.NotNull;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/authentication")
@RequiredArgsConstructor
public class AuthenticationController {
    ////////////////////////////////
    /////////// VARIABLES //////////
    ////////////////////////////////
    private final AuthenticationManager authenticationManager;



    //// AUTHENTICATION ENDPOINT (POST) ///
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(
            @NotNull(message = "Login info cannot be null.") @RequestBody AuthenticateUserDTO authenticateUserDTO) {
        Authentication authentication = authenticationManager.authenticate( new UsernamePasswordAuthenticationToken(authenticateUserDTO.getUsername(), authenticateUserDTO.getPassword()) );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return ResponseEntity.ok("User authenticated successfully!");
    }
}
