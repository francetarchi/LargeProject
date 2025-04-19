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


@Data
@Document(collection = "users")
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    private ObjectId _id;

    private Name name;

    private Location location;

    private String email;
    private String telephone;

    private Login login;

    private Registered registered;
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
    // Effettua alcune operazioni per rendere consistente un utente appena creato
    public void adjustFieldsForCreation(final String encodedPassword) {
        // setto a null i campi che non servono per un utente appena creato
        this.set_id(null);
        this.getRegistered().setDateTime(null);
        this.setReviews(new ArrayList<>());
        this.setLikes(new ArrayList<>());
        this.setDislikes(new ArrayList<>());

        // setto la password codificata
        this.login.setPassword(encodedPassword);

        // setto correttamente la data di registrazione e la data di nascita
        this.adjustDates('C');

        // tolgo gli spazi bianchi da tutti i campi per evitare inconsistenze nel database
        this.trimAllFields();
    }

    // Effettua alcune operazioni per rendere consistente un utente appena aggiornato
    public void adjustFieldsForUpdate() {
        this.adjustDates('U');
        this.trimAllFields();
    }
}
