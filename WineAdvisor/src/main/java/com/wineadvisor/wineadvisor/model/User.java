package com.wineadvisor.wineadvisor.model;

import java.util.ArrayList;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.wineadvisor.wineadvisor.config.PasswordUtils;

import org.apache.coyote.BadRequestException;
import org.bson.types.ObjectId;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Data
@Document(collection = "users")
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    private ObjectId _id;

    @NotBlank(message = "Name info cannot be blank.")
    private Name name;

    @NotBlank(message = "Location info cannot be blank.")
    private Location location;

    @NotBlank(message = "Email cannot be blank.")
    @Email(message = "Email must be a valid email address.")
    @Schema(description = "email", example = "mariorossi@example.com")
    private String email;

    @Pattern(regexp = "^\\+?[0-9\\s\\-()]+$", message = "Telephone must be a valid telephone number.")
    @Schema(description = "telephone", example = "+39 3331234567")
    private String telephone;
    
    @NotBlank(message = "Login info cannot be blank.")
    private Login login;

    private Registered registered;
    
    private Dob dob;

    private Picture picture;

    private ArrayList<Review> reviews;
    
    

    ///////////// METODI /////////////
    // Controlla che i campi obbligatori siano presenti
    public void checkDataPresence() throws BadRequestException {
        
    }

    // Controlla che i campi siano formattati correttamente
    public void checkDataPatterns() throws BadRequestException {
        // if (this.getName() != null) {
        //     if ((this.getName().getTitle() != null || this.getName().getTitle().isEmpty()) && this.getName().getTitle() != "Mr." && this.getName().getTitle() != "Miss.") {
        //         throw new BadRequestException("Title must be one between \"Mr.\" and \"Miss.\".");
        //     }
        //     if (this.getName().getFirst() == null || !this.getName().getFirst().matches("^[a-zA-Z\\s\\-']+$")) {
        //         throw new BadRequestException("First name must contain only letters, spaces, hyphens, and apostrophes.");
        //     }
        //     if (this.getName().getLast() == null || !this.getName().getLast().matches("^[a-zA-Z\\s\\-']+$")) {
        //         throw new BadRequestException("Last name must contain only letters, spaces, hyphens, and apostrophes.");
        //     }
        // }
        // if (!this.getEmail().matches("^[\\w._%+-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$")) {
        //     throw new BadRequestException("Email must be a valid email address.");
        // }
        // if (this.getTelephone() != null && !this.getTelephone().matches("^\\+?[0-9\\s\\-()]+$")) {
        //     throw new BadRequestException("Telephone must be a valid telephone number.");
        // }
        // if (!this.getLogin().getUsername().matches("^[a-zA-Z0-9_]{3,50}$")) {
        //     throw new BadRequestException("Username must be between 3 and 50 characters long and can contain letters, numbers, and underscores.");
        // }
        // if (!PasswordUtils.passwordPatternVerifier(this.getLogin().getPassword())) {
        //     throw new IllegalArgumentException("Password does not meet the minimum requirements: at least 8 characters, 1 digit, 1 lowercase, 1 uppercase, 1 special character among \"!@#$%^&*()-_=+\".");
        // }
        // if (this.getDob() != null) {
        //     if (this.getDob().getDate() != null && !this.getDob().getDate()) {
        //         throw new BadRequestException("Date of birth must be in the format YYYY-MM-DD.");
        //     }
        // }
    }

    // Inserisce la data corrente come data di registrazione e calcola le 'age' correttamente
    public void adjustDates() {
        
    }
}
