package com.wineadvisor.wineadvisor.model.users;

import java.util.ArrayList;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.wineadvisor.wineadvisor.model.users.fields.Dob;
import com.wineadvisor.wineadvisor.model.users.fields.Address;
import com.wineadvisor.wineadvisor.model.users.fields.Name;
import com.wineadvisor.wineadvisor.model.users.fields.WineFavorite;
import com.wineadvisor.wineadvisor.model.utils.Login;
import com.wineadvisor.wineadvisor.model.utils.Picture;
import com.wineadvisor.wineadvisor.model.utils.Registered;
import com.wineadvisor.wineadvisor.model.utils.ReviewEmbedded;

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

    private String gender;

    private Name name;

    private Address address;

    private String email;
    private String telephone;

    private Login login;

    private Registered registered;
    private Dob dob;

    private Picture picture;

    private ArrayList<ReviewEmbedded> reviews;
    private ArrayList<Long> likes;
    private ArrayList<Long> dislikes;

    private ArrayList<WineFavorite> wineFavorites;
    


    ///////////// METODI PRIVATI /////////////
    // Corregge i valori delle date (data di registrazione e data di nascita)
    private void adjustDates(final Character isFrom) {
        if (isFrom.equals('C')) {
            // 'C' indica la C di CRUD, quindi Create: se ho 'C' come argomento, allora sto creando un nuovo utente e devo aggiustare anche la data di registrazione
            this.getRegistered().adjustRegistrationDate();
        }
        this.getDob().adjustDobDate();
    }

    // Eseguo la trim per tutti i campi dello User
    private void trimAllFields(){
        this.name.setTitle(this.name.getTitle().trim());
        this.name.setFirst(this.name.getFirst().trim());
        this.name.setLast(this.name.getLast().trim());

        this.address.getStreet().setNumber(this.address.getStreet().getNumber().trim());
        this.address.getStreet().setName(this.address.getStreet().getName().trim());
        this.address.setCity(this.address.getCity().trim());
        this.address.setRegion(this.address.getRegion().trim());
        this.address.setCountry(this.address.getCountry().trim());
        this.address.setPostcode(this.address.getPostcode().trim());

        this.email = this.email.trim();
        
        this.telephone = this.telephone.trim();

        this.login.setUsername(this.login.getUsername().trim());
        
        this.picture.setLarge(this.picture.getLarge().trim());
        this.picture.setMedium(this.picture.getMedium().trim());
        this.picture.setThumbnail(this.picture.getThumbnail().trim());
    }

    // Effettuo alcune correzioni per rendere consistente un utente appena creato o aggiornato
    private void adjustFields(final Character isFrom) {
        // setto correttamente la data di registrazione e la data di nascita
        this.adjustDates(isFrom);

        // tolgo gli spazi bianchi da tutti i campi per evitare inconsistenze nel database
        this.trimAllFields();
    }



    ///////////// METODI PUBBLICI /////////////
    // Effettua alcune operazioni per rendere consistente un utente appena creato
    public void adjustFieldsForCreation(final String encodedPassword) {
        this.set_id(null);
        this.getRegistered().setDateTime(null);
        this.setReviews(new ArrayList<>());
        this.setLikes(new ArrayList<>());
        this.setDislikes(new ArrayList<>());

        this.login.setPassword(encodedPassword);

        this.adjustFields('C');
    }

    // Effettua alcune operazioni per rendere consistente un utente appena aggiornato
    public void adjustFieldsForUpdate() {
        this.adjustFields('U');
    }
}
