package com.wineadvisor.wineadvisor.DTO.wines;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewFoodDTO {
    @NotBlank(message = "Name is mandatory.")
    @Schema(description = "Name of the food", example = "Pasta")
    private String name;

    @Schema(description = "Image URL of the food", example = "https://example.com/pasta.jpg")
    private String image;
}
