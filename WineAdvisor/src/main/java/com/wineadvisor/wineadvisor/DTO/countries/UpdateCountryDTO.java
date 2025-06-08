package com.wineadvisor.wineadvisor.DTO.countries;

import com.wineadvisor.wineadvisor.DTO.countries.fields.CurrencyDTO;
import com.wineadvisor.wineadvisor.model.countries.Country;

import io.swagger.v3.oas.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCountryDTO {
    @Valid
    @Schema(name = "currency", description = "Currency info of the country to update")
    @JsonProperty("currency")
    private CurrencyDTO currencyDTO;


    
    ///////////// METODI PUBBLICI /////////////
    // Modifica l'oggetto di classe Country passato come argomento sostituendo i campi aggiornabili con i valori aggiornati (quelli dell'istanza attuale (this.)).
    // Ritorna l'utente aggiornato.
    public Country toCountry(Country targetCountry) {
        targetCountry.setCurrency(this.getCurrencyDTO().toCurrency());

        return targetCountry;
    }
}
