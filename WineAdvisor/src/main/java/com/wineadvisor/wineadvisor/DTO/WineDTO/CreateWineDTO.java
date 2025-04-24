package com.wineadvisor.wineadvisor.DTO.WineDTO;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
@JsonPropertyOrder({ "name", "type", "isNatural", "taste", "style" })
public class CreateWineDTO {
    @Positive(message = "Id info cannot be negative.")
    @NotBlank(message = "Name info cannot be blank.")
    @Schema(name = "name", description = "Name info of the new wine", example = "Chardonnay")
    @JsonProperty("name")
    private String name;

    @NotBlank(message = "Type info cannot be blank.")
    @Pattern(
        regexp = "rosso|bianco|rosato|spumante|vino macerato|vino da dessert|vino liquoroso|vino aromatizzato",
        message = "Type must be one of: rosso, bianco, rosato, spumante, vino macerato, vino da dessert, vino liquoroso, vino aromatizzato."
    )
    @Schema(name = "type", description = "Type info of the new wine", example = "white")
    @JsonProperty("type")
    private String type;

    @NotNull(message = "IsNatural info cannot be blank.")
    @Schema(name = "isNatural", description = "IsNatural info of the new wine", example = "true")
    @JsonProperty("isNatural")
    @Field("is_natural")
    private Boolean isNatural;

    @Schema(name = "taste", description = "Taste info of the new wine")
    @Valid
    @JsonProperty("taste")
    private NewTasteDTO taste;

    @Schema(name = "style", description = "Style info of the new wine")
    @Valid
    @JsonProperty("style")
    private NewStyleDTO style;
}
