package com.wineadvisor.wineadvisor.DTO.users.fields;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.wineadvisor.wineadvisor.model.users.fields.Street;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@JsonPropertyOrder({ "number", "name" })
public class StreetDTO {
    @Pattern(regexp = "\\d+[a-zA-Z]?", message = "The house number must start with digits and can optionally end with a letter (just one).")
    @Schema(name = "house number", description = "House number", example = "1a")
    @JsonProperty("house number")
    private String number;

    @Schema(name = "street name", description = "Street name", example = "Via Roma")
    @JsonProperty("street name")
    private String name;



    ///////////// METODI PUBBLICI /////////////
    public Street toStreet() {
        return new Street(this.number, this.name);
    }
}
