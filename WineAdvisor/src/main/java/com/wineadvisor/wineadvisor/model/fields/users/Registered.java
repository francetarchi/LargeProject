package com.wineadvisor.wineadvisor.model.fields.users;

import java.time.LocalDateTime;

import com.wineadvisor.wineadvisor.model.fields.DateTimePattern;

public class Registered extends DateTimePattern {
    ///////////// METODI PUBBLICI /////////////
    // Setta la data di registrazione a quella attuale per aggiornare il campo "age" in automatico
    public void adjustRegistrationDate() {
        this.setDateTime(LocalDateTime.now().withNano((LocalDateTime.now().getNano() / 1_000_000) * 1_000_000));
    }
}
