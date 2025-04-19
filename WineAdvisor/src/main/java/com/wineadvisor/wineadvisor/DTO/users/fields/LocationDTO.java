package com.wineadvisor.wineadvisor.DTO.users.fields;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.wineadvisor.wineadvisor.model.fields.users.Location;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@JsonPropertyOrder({ "street", "city", "region", "country", "postcode" })
public class LocationDTO {
    @Valid
    @Schema(name = "street", description = "Street info of the address")
    @JsonProperty("street")
    private StreetDTO streetDTO;

    @Pattern(regexp = "^[a-zA-ZÀ-ÿ'\\-\\s]+$", message = "City can only contain letters, spaces, apostrophes, and hyphens.")
    @Schema(description = "City of the address", example = "Pisa")
    private String city;

    @NotBlank(message = "Region cannot be blank.")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ'\\-\\s]+$", message = "Region can only contain letters, spaces, apostrophes, and hyphens.")
    @Schema(description = "Region of the address", example = "Toscana")
    private String region;

    @NotBlank(message = "Country cannot be blank.")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ'\\-\\s]+$", message = "Country can only contain letters, spaces, apostrophes, and hyphens.")
    @Schema(description = "Country of the address", example = "Italia")
    private String country;

    @Pattern(regexp = "^[0-9]{4,10}$", message = "Postcode must be between 4 and 10 digits.")
    @Schema(description = "Postcode of the address", example = "56126")
    private String postcode;


    
    ///////////// METODI PUBBLICI /////////////
    public Location toLocation() {
        return new Location(this.streetDTO.toStreet(), this.city, this.region, this.country, this.postcode);
    }
}
