package com.wineadvisor.wineadvisor.config;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordUtils {
    public static Boolean passwordPatternVerifier(String password) {
        // La password deve essere lunga almeno 8 caratteri
        if (password.length() < 8) {
            return false;
        }
        // La password deve contenere almeno un numero
        if (!password.matches(".*\\d.*")) {
            return false;
        }
        // La password deve contenere almeno una lettera minuscola
        if (!password.matches(".*[a-z].*")) {
            return false;
        }
        // La password deve contenere almeno una lettera maiuscola
        if (!password.matches(".*[A-Z].*")) {
            return false;
        }
        // La password deve contenere almeno un carattere speciale
        if (!password.matches(".*[!@#$%^&*()-_=+].*")) {
            return false;
        }
        return true;
    }

    public static PasswordEncoder passwordEncoder() {
        // Setting BCRYPT as password encoder algorithm.
        // Call SecurityConfig.passwordEncoder() to create an instance of an encoder using BCRYPT algorithm.
        // Call .encode("password") on the instance to encode a "password".
        return new BCryptPasswordEncoder();
    }
}
