package com.wineadvisor.wineadvisor.DTO.WineDTO;

import java.util.ArrayList;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder({ "name", "description", "interestingFacts", "body", "acidity", "food", "grapes", "baselineStructure" })
public class NewStyleDTO {
    @NotBlank(message = "Name is mandatory.")
    @Schema(description = "Name of the wine style", example = "Vino Nobile di Montepulciano")
    @JsonProperty("name")
    private String name;

    @Schema(description = "Description of the wine style", example = "Vino Nobile di Montepulciano is a red wine from Tuscany, Italy, made primarily from Sangiovese grapes.")
    @JsonProperty("description")
    private String description;

    @Field("interesting_facts")
    @Schema(description = "Interesting facts about the wine style", example = "[\"Vino Nobile di Montepulciano is one of the oldest wines in Italy.\", \"It was the first Italian wine to receive the Denominazione di Origine Controllata e Garantita (DOCG) status.\"]")
    @JsonProperty("interestingFacts")
    private ArrayList<String> interestingFacts;

    @NotNull(message = "Body is mandatory.")
    @PositiveOrZero(message = "Body must be a positive number.")
    @Schema(description = "Body of the wine style", example = "3")
    @Min(value = 0, message = "Body must be at least 0.")
    @Max(value = 5, message = "Body must be at most 5.")
    private Integer body;

    @NotNull(message = "Acidity is mandatory.")
    @PositiveOrZero(message = "Acidity must be a positive number.")
    @Schema(description = "Acidity of the wine style", example = "3")
    @Min(value = 0, message = "Acidity must be at least 0.")
    @Max(value = 5, message = "Acidity must be at most 5.")
    private Integer acidity;

    @Schema(description = "Food pairings for the wine style")
    @Valid
    private ArrayList<NewFoodDTO> food;

    @Schema(description = "Grapes used in the wine style")
    @Valid
    private ArrayList<NewGrapeDTO> grapes;

    @Schema(description = "Baseline structure of the wine style")
    @Valid
    @Field("baseline_structure")
    private NewBaselineStructureDTO baselineStructure;
}