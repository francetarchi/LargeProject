package com.wineadvisor.wineadvisor.model.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;

import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AccessLevel;

@Data
@NoArgsConstructor
public abstract class DateTimePattern {
    @Field(name = "date")
    private Instant dateTime;

    @Setter(AccessLevel.NONE)
    private Integer age;


    ///////////// METODI /////////////
    // Setter del campo 'dateTime' che in automatico calcola e modifica il campo 'age' ogni volta che viene modificato il campo 'dateTime'
    public void setDateTime(Instant dateTime) {
        this.dateTime = dateTime;
        if (dateTime != null) {
            // Per calcolare l'età, potresti aver bisogno di convertire l'Instant in LocalDate basandoti su un fuso orario specifico (es. quello del server o UTC)
            LocalDateTime localDateTime = LocalDateTime.ofInstant(dateTime, ZoneId.of("UTC"));
            this.age = Period.between(localDateTime.toLocalDate(), LocalDate.now(ZoneId.of("UTC"))).getYears();
        } else {
            this.age = null;
        }
    }
}
