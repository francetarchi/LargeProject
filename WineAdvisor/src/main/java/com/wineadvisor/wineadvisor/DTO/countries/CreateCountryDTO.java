package com.wineadvisor.wineadvisor.DTO.countries;

import java.util.ArrayList;

import com.wineadvisor.wineadvisor.DTO.countries.fields.CurrencyDTO;
import com.wineadvisor.wineadvisor.model.countries.Country;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({ "name", "currency" })
public class CreateCountryDTO {
    @NotBlank(message = "Name cannot be blank.")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ'\\-\\s]+$", message = "Name must contain only letters, spaces, hyphens, and apostrophes.")
    @Schema(description = "Name of the country to create", example = "Italy")
    private String name;
    
    @Valid
    @JsonProperty("currency")
    @Schema(name = "currency", description = "Currency info of the country")
    private CurrencyDTO currencyDTO;



    ///////////// METODI PUBBLICI /////////////
    public Country toCountry() {
        Country country = new Country(
            null, // _id
            this.name, // name
            this.currencyDTO.toCurrency(), // currency
            new ArrayList<>() // top_vintages
        );

        return country;
    }
}
