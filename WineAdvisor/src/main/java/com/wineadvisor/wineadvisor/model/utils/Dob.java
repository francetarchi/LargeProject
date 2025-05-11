package com.wineadvisor.wineadvisor.model.utils;

import java.time.temporal.ChronoUnit;

public class Dob extends DateTimePattern {
    ///////////// METODI PUBBLICI /////////////
    // Setta la data di nascita prendendo quella passata nella richiesta HTTP per aggiornare il campo 'age' in automatico
    public void adjustDobDate() {
        if (this.equals(null) || this.getDateTime() == null) {
            this.setDateTime(null);
        } else {
            // TODO: DA ELIMINARE SE FUNZIONA TUTTO: this.setDateTime(this.getDateTime().withNano((this.getDateTime().getNano() / 1_000_000) * 1_000_000));
            this.setDateTime(this.getDateTime().truncatedTo(ChronoUnit.MILLIS));
        }
    }
}
