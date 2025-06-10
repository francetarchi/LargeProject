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
    @Schema(description = "Name of the wine style", example = "Sauvignon Blanc")
    private String name;

    @Schema(description = "Description of the wine style", example = "Sauvignon Blanc is a white grape variety known for its crisp acidity and fresh, aromatic profile. It often displays vibrant flavors of citrus, green apple, and tropical fruits, with a characteristic herbal note reminiscent of grass or freshly cut herbs.")
    private String description;

    @Schema(description = "Interesting facts about the wine style", example = "[\"Originally from Bordeaux, France, Sauvignon Blanc is now grown in many wine regions around the world, including New Zealand, where it has become a signature grape.\", \"Sauvignon Blanc pairs exceptionally well with dishes like goat cheese, shellfish, and salads, thanks to its zesty acidity that complements the freshness of these foods.\"]")
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
