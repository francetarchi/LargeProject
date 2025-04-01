package com.wineadvisor.wineadvisor.model;

import java.time.LocalDateTime;
import java.time.Period;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.Setter;
import lombok.AccessLevel;

@Data
public abstract class DateTimePattern {
    @PastOrPresent
    // @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z$", message = "Date must follow the format 'yyyy-MM-ddTHH:mm:ssZ'")
    @Schema(description = "DateTime", example = "1970-01-01T00:00:00Z")
    private LocalDateTime dateTime;

    @Schema(description = "Age", example = "55")
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
