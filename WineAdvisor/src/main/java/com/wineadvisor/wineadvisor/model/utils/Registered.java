package com.wineadvisor.wineadvisor.model.utils;

import java.time.Clock;
import java.time.temporal.ChronoUnit;

public class Registered extends DateTimePattern {
    ///////////// METODI PUBBLICI /////////////
    // Setta la data di registrazione a quella attuale per aggiornare il campo "age" in automatico
    public void adjustRegistrationDate() {
        this.setDateTime(Clock.systemUTC().instant().truncatedTo(ChronoUnit.MILLIS));
    }
}
