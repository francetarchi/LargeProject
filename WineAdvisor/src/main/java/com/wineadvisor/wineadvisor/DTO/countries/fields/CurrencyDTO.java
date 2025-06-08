package com.wineadvisor.wineadvisor.DTO.countries.fields;

import com.wineadvisor.wineadvisor.model.utils.Currency;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyDTO {
    @Pattern(regexp = "^[A-Z]{3}$", message = "Code must be a 3-letter uppercase string.")
    @Schema(description = "Code of the currency of the country.", example = "EUR")
    private String code;

    @NotBlank(message = "Name of the currency cannot be blank.")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ'\\-\\s]+$", message = "Name must contain only letters, spaces, hyphens, and apostrophes.")
    @Schema(description = "Name of the currency of the country.", example = "Euro")
    private String name;
    
    @NotBlank(message = "Prefix of the currency cannot be blank.")
    @Schema(description = "Prefix of the currency of the country.", example = "€")
    private String prefix;



    ///////////// METODI PUBBLICI /////////////
    public Currency toCurrency() {
        return new Currency(this.getCode(), this.getName(), this.getPrefix());
    }
}
