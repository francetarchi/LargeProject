package com.wineadvisor.wineadvisor.DTO.styles;

import java.util.ArrayList;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class StyleDTO {
    @NotBlank(message = "Name is mandatory.")
    @Schema(description = "Name of the wine style", example = "Vino Nobile di Montepulciano")
    private String name;

    @Schema(description = "Description of the wine style", example = "Vino Nobile di Montepulciano is a red wine from Tuscany, Italy, made primarily from Sangiovese grapes.")
    private String description;

    @Schema(description = "Interesting facts about the wine style", example = "[\"Vino Nobile di Montepulciano is one of the oldest wines in Italy.\", \"It was the first Italian wine to receive the Denominazione di Origine Controllata e Garantita (DOCG) status.\"]")
    private ArrayList<String> interestingFacts;

    @Schema(description = "Food pairings for the wine style")
    @Valid
    private ArrayList<FoodDTO> food;

    @Schema(description = "Grapes used in the wine style", example = "[\"Sangiovese\", \"Canaiolo\"]")
    private ArrayList<String> grapes;

    @Schema(description = "Taste of the wine style")
    @Valid
    private TasteDTO taste;
}
