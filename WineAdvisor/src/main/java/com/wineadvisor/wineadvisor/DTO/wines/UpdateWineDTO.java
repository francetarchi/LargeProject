package com.wineadvisor.wineadvisor.DTO.wines;

import org.springframework.data.mongodb.core.mapping.Field;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateWineDTO {
    @NotBlank(message = "Name info cannot be blank.")
    @Schema(description = "Name info of the wine", example = "Sassella Ultimi Raggi")
    private String name;

    @NotBlank(message = "Type info cannot be blank.")
    @Pattern(
        regexp = "rosso|bianco|rosato|spumante|vino macerato|vino da dessert|vino liquoroso|vino aromatizzato",
        message = "Type must be one of: rosso, bianco, rosato, spumante, vino macerato, vino da dessert, vino liquoroso, vino aromatizzato."
    )
    @Schema(description = "Type info of the wine", example = "bianco")
    private String type;

    @NotNull(message = "IsNatural info cannot be blank.")
    @Schema(description = "IsNatural info of the wine", example = "false")
    @Field("is_natural")
    private Boolean isNatural;

    @Schema(description = "Style name of the wine", example = "Nebbiolo Italiano")
    private String style;
}
