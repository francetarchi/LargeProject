package com.wineadvisor.wineadvisor.model.users.fields;

import com.wineadvisor.wineadvisor.model.utils.DateTimePattern;

public class Dob extends DateTimePattern {
    ///////////// METODI PUBBLICI /////////////
    // Setta la data di nascita prendendo quella passata nella richiesta HTTP per aggiornare il campo 'age' in automatico
    public void adjustDobDate() {
        if (this.equals(null) || this.getDateTime() == null) {
            this.setDateTime(null);
        } else {
            this.setDateTime(this.getDateTime().withNano((this.getDateTime().getNano() / 1_000_000) * 1_000_000));
        }
    }
}
