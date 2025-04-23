package com.wineadvisor.wineadvisor.DTO.WineDTO;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.wineadvisor.wineadvisor.model.fields.wines.Style;
import com.wineadvisor.wineadvisor.model.fields.wines.Taste;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder({ "wineId", "name", "type", "isNatural", "taste", "style" })
public class UpdateWineDTO {
    @NotNull(message = "Wine ID cannot be blank.")
    @Schema(name = "wineId", description = "Wine ID info of the new vintage", example = "1")
    @JsonProperty("wineId")
    private Long wineId;

    @NotBlank(message = "Name info cannot be blank.")
    @Schema(name = "name", description = "Name info of the new wine", example = "Chardonnay")
    @JsonProperty("name")
    private String name;

    @NotBlank(message = "Type info cannot be blank.")
    @Schema(name = "type", description = "Type info of the new wine", example = "white")
    @JsonProperty("type")
    private String type;

    @NotNull(message = "IsNatural info cannot be blank.")
    @Schema(name = "isNatural", description = "IsNatural info of the new wine", example = "true")
    @JsonProperty("isNatural")
    @Field("is_natural")
    private Boolean isNatural;

    @Schema(name = "taste", description = "Taste info of the new wine")
    @JsonProperty("taste")
    private Taste taste;

    @Schema(name = "style", description = "Style info of the new wine")
    @JsonProperty("style")
    private Style style;
}
