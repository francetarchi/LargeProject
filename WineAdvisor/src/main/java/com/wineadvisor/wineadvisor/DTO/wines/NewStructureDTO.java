package com.wineadvisor.wineadvisor.DTO.wines;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewStructureDTO {
    @PositiveOrZero(message = "Acidity must be a positive number.")
    @DecimalMin(value = "0.0", inclusive = true, message = "Acidity must be at least 0.")
    @DecimalMax(value = "5.0", inclusive = true, message = "Acidity must be at most 5.")
    @Schema(description = "Wine acidity", example = "0.5")
    private Double acidity;

    @PositiveOrZero(message = "Fizziness must be a positive number.")
    @DecimalMin(value = "0.0", inclusive = true, message = "Fizziness must be at least 0.")
    @DecimalMax(value = "5.0", inclusive = true, message = "Fizziness must be at most 5.")
    @Schema(description = "Wine fizziness", example = "0.5")
    private Double fizziness;

    @PositiveOrZero(message = "Intensity must be a positive number.")
    @DecimalMin(value = "0.0", inclusive = true, message = "Intensity must be at least 0.")
    @DecimalMax(value = "5.0", inclusive = true, message = "Intensity must be at most 5.")
    @Schema(description = "Wine intensity", example = "0.5")
    private Double intensity;

    @PositiveOrZero(message = "Sweetness must be a positive number.")
    @DecimalMin(value = "0.0", inclusive = true, message = "Sweetness must be at least 0.")
    @DecimalMax(value = "5.0", inclusive = true, message = "Sweetness must be at most 5.")
    @Schema(description = "Wine sweetness", example = "0.5")
    private Double sweetness;

    @PositiveOrZero(message = "Tannin must be a positive number.")
    @DecimalMin(value = "0.0", inclusive = true, message = "Tannin must be at least 0.")
    @DecimalMax(value = "5.0", inclusive = true, message = "Tannin must be at most 5.")
    @Schema(description = "Wine tannin", example = "0.5")
    private Double tannin;
}
