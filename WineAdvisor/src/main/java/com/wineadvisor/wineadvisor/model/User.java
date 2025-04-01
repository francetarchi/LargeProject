package com.wineadvisor.wineadvisor.model;

import java.time.LocalDateTime;
import java.util.ArrayList;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import com.fasterxml.jackson.annotation.JsonInclude;

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
    // Controlla se la data di registrazione è presente e se è quella odierna: se trova un problema, lo corregge inserendo il timestamp attuale
    public void adjustDates() {
        if (this.getRegistered() == null || this.getRegistered().getDateTime() == null || this.getRegistered().getDateTime().toLocalDate().isBefore(LocalDateTime.now().toLocalDate())) {
            this.getRegistered().setDateTime(LocalDateTime.now());
        }
    }
}
