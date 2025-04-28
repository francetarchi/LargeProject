package com.wineadvisor.wineadvisor.DTO.reviews;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder({ "username", "rating", "text" })
public class UpdateReviewDTO {
    @NotBlank(message = "Username cannot be null.")
    @Valid
    @Schema(description = "Username of the user who created the review.", example = "silverelephant535")
    @JsonProperty("username")
    private String username;

    @PositiveOrZero(message = "Rating must be a positive number.")
    @DecimalMin(value = "0.0", inclusive = true, message = "Rating must be at least 0.")
    @DecimalMax(value = "5.0", inclusive = true, message = "Rating must be at most 5.")
    @NotNull(message = "Rating info cannot be blank.")
    @Valid
    @Schema(name = "rating", description = "Rating info of the new vintage", example = "4.5")
    @JsonProperty("rating")
    private Double rating;

    @Schema(name = "text", description = "Text info of the new review", example = "This wine is amazing!")
    @JsonProperty("text")
    private String text;
}
