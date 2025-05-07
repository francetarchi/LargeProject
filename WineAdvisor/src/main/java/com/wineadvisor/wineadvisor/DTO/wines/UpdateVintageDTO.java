package com.wineadvisor.wineadvisor.DTO.wines;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateVintageDTO {
    @Positive(message = "Wine ID must be a positive number.")
    @NotNull(message = "Wine ID cannot be blank.")
    @Schema(description = "Wine ID info of the new vintage", example = "1")
    private Long wineId;

    @PositiveOrZero(message = "Year info must be a positive number.")
    @Schema(description = "Year info of the new vintage", example = "2000")
    private Integer year;
    
    @Positive(message = "Price info must be a positive number.")
    @NotNull(message = "Price info cannot be blank.")
    @Schema(description = "Price info of the new vintage", example = "19.99")
    private Double price;

    @Schema(description = "Image info of the new vintage", example = "https://example.com/image.jpg")
    private String image;
}
