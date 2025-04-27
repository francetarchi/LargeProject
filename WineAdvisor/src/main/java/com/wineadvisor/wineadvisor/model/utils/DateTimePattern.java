package com.wineadvisor.wineadvisor.model.utils;

import java.time.LocalDateTime;
import java.time.Period;

import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AccessLevel;

@Data
@NoArgsConstructor
public abstract class DateTimePattern {
    @Field(name = "date")
    private LocalDateTime dateTime;

    @Setter(AccessLevel.NONE)
    private Integer age;


    ///////////// METODI /////////////
    // Setter del campo 'dateTime' che in automatico calcola e modifica il campo 'age' ogni volta che viene modificato il campo 'dateTime'
    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
        if (dateTime != null) {
            this.age = Period.between(dateTime.toLocalDate(), LocalDateTime.now().toLocalDate()).getYears();
        } else {
            this.age = null;
        }
    }
}
