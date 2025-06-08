package com.wineadvisor.wineadvisor.model.admin;

import com.wineadvisor.wineadvisor.model.utils.Name;
import com.wineadvisor.wineadvisor.model.utils.Login;
import com.wineadvisor.wineadvisor.model.utils.Registered;
import com.wineadvisor.wineadvisor.model.utils.Dob;
import com.wineadvisor.wineadvisor.model.utils.Picture;

import com.fasterxml.jackson.annotation.JsonInclude;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "admins")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Admin {
    @Id
    private ObjectId _id;

    private String gender;

    private Name name;

    private String email;
    private String telephone;

    private Login login;

    private Registered registered;
    private Dob dob;

    private Picture picture;



    ///////////// METODI PRIVATI /////////////
    // Corregge i valori delle date (data di registrazione e data di nascita)
    private void adjustDates(final Character isFrom) {
        if (isFrom.equals('C')) {
            // 'C' indica la C di CRUD, quindi Create: se ho 'C' come argomento, allora sto creando un nuovo utente e devo aggiustare anche la data di registrazione
            this.getRegistered().adjustRegistrationDate();
        }
        this.getDob().adjustDobDate();
    }

    // Esegue la trim per tutti i campi dell'Admin
    private void trimAllFields(){
        this.gender = this.gender.trim();
        
        this.name.setTitle(this.name.getTitle().trim());
        this.name.setFirst(this.name.getFirst().trim());
        this.name.setLast(this.name.getLast().trim());

        this.email = this.email.trim();
        this.telephone = this.telephone.trim();

        this.login.setUsername(this.login.getUsername().trim());
        
        this.picture.setLarge(this.picture.getLarge().trim());
        this.picture.setMedium(this.picture.getMedium().trim());
        this.picture.setThumbnail(this.picture.getThumbnail().trim());
    }

    // Effettua ulteriori correzioni per rendere consistente un admin appena creato o aggiornato
    private void adjustFields(final Character isFrom) {
        // setto correttamente la data di registrazione e la data di nascita
        this.adjustDates(isFrom);

        // tolgo gli spazi bianchi da tutti i campi per evitare inconsistenze nel database
        this.trimAllFields();
    }



    ///////////// METODI PUBBLICI /////////////
    // Effettua alcune operazioni per rendere consistente un admin appena creato
    public void adjustFieldsForCreation(final String encodedPassword) {
        this.set_id(null);
        this.getRegistered().setDateTime(null);
        this.login.setPassword(encodedPassword);

        this.adjustFields('C');
    }

    // Effettua alcune operazioni per rendere consistente un admin appena aggiornato
    public void adjustFieldsForUpdate() {
        this.adjustFields('U');
    }
}
