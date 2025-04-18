package com.wineadvisor.wineadvisor.model;

import java.util.ArrayList;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.wineadvisor.wineadvisor.model.fields.ReviewEmbedded;
import com.wineadvisor.wineadvisor.model.fields.users.Dob;
import com.wineadvisor.wineadvisor.model.fields.users.Location;
import com.wineadvisor.wineadvisor.model.fields.users.Login;
import com.wineadvisor.wineadvisor.model.fields.users.Name;
import com.wineadvisor.wineadvisor.model.fields.users.Picture;
import com.wineadvisor.wineadvisor.model.fields.users.Registered;

import org.bson.types.ObjectId;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Data
@Document(collection = "users")
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    private ObjectId _id;

    @NotNull(message = "Name info cannot be blank.")
    @Valid
    private Name name;

    @NotNull(message = "Location info cannot be blank.")
    @Valid
    private Location location;

    @NotBlank(message = "Email cannot be blank.")
    @Email(message = "Email must be a valid email address.")
    @Schema(description = "email", example = "mariorossi@example.com")
    private String email;

    @Pattern(regexp = "^\\+?[0-9\\s\\-()]+$", message = "Telephone must be a valid telephone number.")
    @Schema(description = "telephone", example = "+39 3331234567")
    private String telephone;
    
    @NotNull(message = "Login info cannot be blank.")
    @Valid
    private Login login;

    @Valid
    private Registered registered;
    
    @Valid
    private Dob dob;

    private Picture picture;

    private ArrayList<ReviewEmbedded> reviews;
    private ArrayList<Long> likes;
    private ArrayList<Long> dislikes;
    


    ///////////// METODI PRIVATI /////////////
    // Corregge i valori delle date (data di registrazione e data di nascita)
    private void adjustDates(final Character isFrom) {
        if (isFrom.equals('C')) {
            // 'C' indica la C di CRUD, quindi Create: se ho 'C' come argomento, allora sto creando un nuovo utente e devo aggiustare anche la data di registrazione
            this.getRegistered().adjustRegistrationDate();
        }
        this.getDob().adjustDobDate();
    }

    // Eseguo la trim per tutti i campi passati come argomento
    private void trimAllFields(){
        this.name.setTitle(this.name.getTitle().trim());
        this.name.setFirst(this.name.getFirst().trim());
        this.name.setLast(this.name.getLast().trim());

        this.location.getStreet().setNumber(this.location.getStreet().getNumber().trim());
        this.location.getStreet().setName(this.location.getStreet().getName().trim());
        this.location.setCity(this.location.getCity().trim());
        this.location.setRegion(this.location.getRegion().trim());
        this.location.setCountry(this.location.getCountry().trim());
        this.location.setPostcode(this.location.getPostcode().trim());


        this.email = this.email.trim();
        
        this.telephone = this.telephone.trim();

        this.login.setUsername(this.login.getUsername().trim());
        this.login.setPassword(this.login.getPassword().trim());
        
        this.picture.setLarge(this.picture.getLarge().trim());
        this.picture.setMedium(this.picture.getMedium().trim());
        this.picture.setThumbnail(this.picture.getThumbnail().trim());
    }



    ///////////// METODI PUBBLICI /////////////
    // Corregge i valori da ritoccare durante la creazione di un nuovo utente per non rendere inconsistente il database
    public void adjustFieldsForCreation(final String encodedPassword) {
        this._id = null;
        this.reviews = new ArrayList<>();
        this.login.setPassword(encodedPassword);
        this.adjustDates('C');
        this.trimAllFields();
    }

    // Corregge i valori da ritoccare durante la modifica di un utente per non rendere inconsistente il database
    public void adjustFieldsForUpdate() {
        this.adjustDates('U');;
        this.trimAllFields();
    }
}