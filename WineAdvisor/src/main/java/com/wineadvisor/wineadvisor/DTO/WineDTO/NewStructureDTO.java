package com.wineadvisor.wineadvisor.DTO.WineDTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder({ "acidity", "fizziness", "intensity", "sweetness", "tannin" })
public class NewStructureDTO {
    @PositiveOrZero(message = "Acidity must be a positive number.")
    @DecimalMin(value = "0.0", inclusive = true, message = "Acidity must be at least 0.")
    @DecimalMax(value = "5.0", inclusive = true, message = "Acidity must be at most 5.")
    @JsonProperty("acidity")
    private Double acidity;

    @PositiveOrZero(message = "Fizziness must be a positive number.")
    @DecimalMin(value = "0.0", inclusive = true, message = "Fizziness must be at least 0.")
    @DecimalMax(value = "5.0", inclusive = true, message = "Fizziness must be at most 5.")
    @JsonProperty("fizziness")
    private Double fizziness;

    @PositiveOrZero(message = "Intensity must be a positive number.")
    @DecimalMin(value = "0.0", inclusive = true, message = "Intensity must be at least 0.")
    @DecimalMax(value = "5.0", inclusive = true, message = "Intensity must be at most 5.")
    @JsonProperty("intensity")
    private Double intensity;

    @PositiveOrZero(message = "Sweetness must be a positive number.")
    @DecimalMin(value = "0.0", inclusive = true, message = "Sweetness must be at least 0.")
    @DecimalMax(value = "5.0", inclusive = true, message = "Sweetness must be at most 5.")
    @JsonProperty("sweetness")
    private Double sweetness;

    @PositiveOrZero(message = "Tannin must be a positive number.")
    @DecimalMin(value = "0.0", inclusive = true, message = "Tannin must be at least 0.")
    @DecimalMax(value = "5.0", inclusive = true, message = "Tannin must be at most 5.")
    @JsonProperty("tannin")
    private Double tannin;
}
