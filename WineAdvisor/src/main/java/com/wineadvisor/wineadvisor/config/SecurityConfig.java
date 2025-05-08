package com.wineadvisor.wineadvisor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import com.wineadvisor.wineadvisor.DTO.utils.PasswordDTO;
import com.wineadvisor.wineadvisor.service.AuthenticationService;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {
    ////////////////////////////////
    /////////// VARIABLES //////////
    ////////////////////////////////
    private final AuthenticationService authenticationService;


    
    ////////////////////////////////
    //////// PUBLIC METHODS ////////
    ////////////////////////////////
    
    /////////////////////////////////
    //// Authentication settings ////
    
    // Imposta il provider di autenticazione per l'autenticazione degli utenti
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(authenticationService);
        authProvider.setPasswordEncoder(PasswordDTO.passwordEncoder());
        return authProvider;
    }

    // Imposta il gestore di autenticazione per l'autenticazione degli utenti
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    
    // TEST
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/authentication/login").anonymous()
            .requestMatchers(HttpMethod.POST, "/api/users").anonymous()
            .requestMatchers(HttpMethod.POST, "/api/wineries").anonymous()
            .anyRequest().authenticated()
        )
        .authenticationProvider(authenticationProvider())
        .httpBasic(); // <--- Questa è la riga giusta per abilitare Basic Auth

    return http.build();
}


    //// END of auth. settings ////
    ///////////////////////////////
}
