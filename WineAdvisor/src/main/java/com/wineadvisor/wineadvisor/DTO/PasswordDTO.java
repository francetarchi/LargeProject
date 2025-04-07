package com.wineadvisor.wineadvisor.DTO;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordDTO {
    @Schema(description = "Old password", example = "oldPass123!")
    private String oldPass;

    @NotBlank(message = "New password is required")
    @Schema(description = "New password", example = "newPass123!")
    private String newPass;

    @NotBlank(message = "Confirm password is required")
    @Schema(description = "Confirm new password", example = "newPass123!")
    private String confirmPass;


    ///////////// METODI /////////////
    // Controlla che la password rispetti un determinato pattern: ritorna true se lo rispetta, false se viola almeno un vincolo.
    public Boolean passwordPatternVerifier() {
        // La password deve essere lunga almeno 8 caratteri
        if (this.newPass.length() < 8) {
            return false;
        }
        // La password deve contenere almeno un numero
        if (!this.newPass.matches(".*\\d.*")) {
            return false;
        }
        // La password deve contenere almeno una lettera minuscola
        if (!this.newPass.matches(".*[a-z].*")) {
            return false;
        }
        // La password deve contenere almeno una lettera maiuscola
        if (!this.newPass.matches(".*[A-Z].*")) {
            return false;
        }
        // La password deve contenere almeno un carattere speciale
        if (!this.newPass.matches(".*[!@#$%^&*()-_=+].*")) {
            return false;
        }
        
        return true;
    }

    // Funzione che ritorna un oggetto di tipo PasswordEncoder che utilizza BCRYPT come algoritmo di sicurezza.
    // Chiamare PasswordDTO.passwordEncoder() per creare un'istanza di tipo PasswordEncoder.
    // Chiamare passwordEncoderInstance.encode("password") per codificare una "password".
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
