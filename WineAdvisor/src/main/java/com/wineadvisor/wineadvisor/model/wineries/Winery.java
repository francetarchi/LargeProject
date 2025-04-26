package com.wineadvisor.wineadvisor.model.wineries;

import java.util.ArrayList;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.wineadvisor.wineadvisor.model.utils.Login;
import com.wineadvisor.wineadvisor.model.utils.Picture;
import com.wineadvisor.wineadvisor.model.utils.Registered;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Document(collection = "wineries")
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
public class Winery {
    @Id
    private Long _id;

    private String name;

    private String address;
    private String city;
    private String zipcode;
    private String province;
    private String region;
    private String country;

    private String telephone;
    private String email;

    private String website;
    private String facebook;
    private String instagram;
    
    private Login login;

    private Registered registered;
    
    private Picture picture;    // Profile picture of the winery
    private ArrayList<String> images;   // Images of the winery (gallery)



    ///////////// METODI PRIVATI /////////////
    // Corregge i valori delle date (data di registrazione e data di nascita)
    private void adjustDates(final Character isFrom) {
        if (isFrom.equals('C')) {
            // 'C' indica la C di CRUD, quindi Create: se ho 'C' come argomento, allora sto creando un nuovo utente e devo aggiustare anche la data di registrazione
            this.getRegistered().adjustRegistrationDate();
        }
    }

    // Eseguo la trim per tutti i campi della Winery
    private void trimAllFields(){
        this.name = this.name.trim();
        
        this.address = this.address.trim();
        this.city = this.city.trim();
        this.zipcode = this.zipcode.trim();
        this.province = this.province.trim();
        this.region = this.region.trim();
        this.country = this.country.trim();
        
        this.telephone = this.telephone.trim();
        this.email = this.email.trim();
        
        this.website = this.website.trim();
        this.facebook = this.facebook.trim();
        this.instagram = this.instagram.trim();

        this.login.setUsername(this.login.getUsername().trim());
        
        this.picture.setLarge(this.picture.getLarge().trim());
        this.picture.setMedium(this.picture.getMedium().trim());
        this.picture.setThumbnail(this.picture.getThumbnail().trim());
    }

    // Effettuo alcune correzioni per rendere consistente una winery appena creata o aggiornata
    private void adjustFields(final Character isFrom) {
        // setto correttamente la data di registrazione e la data di nascita
        this.adjustDates(isFrom);

        // tolgo gli spazi bianchi da tutti i campi per evitare inconsistenze nel database
        this.trimAllFields();
    }
    
    
    
    ///////////// METODI PUBBLICI ////////////
    // Effettua alcune operazioni per rendere consistente una winery appena creata
    public void adjustFieldsForCreation(final String encodedPassword) {
        this.set_id(null);
        this.getRegistered().setDateTime(null);
        this.setImages(new ArrayList<>());

        this.login.setPassword(encodedPassword);

        this.adjustFields('C');
    }

    // Effettua alcune operazioni per rendere consistente una winery appena aggiornata
    public void adjustFieldsForUpdate() {
        this.adjustFields('U');
    }
}
