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
    
    // Imposta i vari settaggi necessari per l'autenticazione degli utenti e dei loro privilegi
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())   // Permette tutte le richieste senza autenticazione
            .csrf(csrf -> csrf.disable())   // Disabilito CSRF (utile per testare con Postman)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))   // Nessuna sessione
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/authentication/login").anonymous()   // Permetto l'accesso a /api/authentication/** solamente ad utenti non autenticati (endpoint di login e logout)
                .requestMatchers(HttpMethod.POST, "/api/users").anonymous()     // Permetto l'accesso a /api/users solamente ad utenti non autenticati per le richieste di tipo POST (endpoint di registrazione di un utente)
                .requestMatchers(HttpMethod.POST, "/api/wineries").anonymous()  // Permetto l'accesso a /api/wineries solamente ad utenti non autenticati per le richieste di tipo POST (endpoint di registrazione di un'azienda vinicola)
                .anyRequest().authenticated()   // Tutte le altre richieste richiedono autenticazione (senza specificare il ruolo)
            )
            .authenticationProvider(authenticationProvider())
            .httpBasic(httpBasic -> httpBasic.realmName("WineAdvisor"));    // Configuro l'autenticazione Basic per Postman
        
        return http.build();
    }

    //// END of auth. settings ////
    ///////////////////////////////
}
