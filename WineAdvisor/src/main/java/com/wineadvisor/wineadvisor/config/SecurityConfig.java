package com.wineadvisor.wineadvisor.config;

import org.springframework.security.config.Customizer; //TEST
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
    return http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/test-auth").authenticated()
            .anyRequest().permitAll()
        )
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authenticationProvider(authenticationProvider())
        .httpBasic(Customizer.withDefaults())
        .build();
}



    //// END of auth. settings ////
    ///////////////////////////////
}
