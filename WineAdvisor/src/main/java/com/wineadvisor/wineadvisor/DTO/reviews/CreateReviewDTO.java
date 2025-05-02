package com.wineadvisor.wineadvisor.DTO.reviews;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateReviewDTO {
    @NotBlank(message = "Username cannot be null.")
    @Schema(description = "Username of the user who created the review.", example = "silverelephant535")
    private String username;

    @Positive(message = "Wine ID must be a positive number.")
    @NotNull(message = "Wine ID cannot be blank.")
    @Schema(description = "Wine ID info of the new vintage", example = "542")
    private Long wineId;

    @PositiveOrZero(message = "Year must be a positive number.")
    @NotNull(message = "Year info cannot be blank.")
    @Schema(description = "Year info of the new vintage", example = "2000")
    private Integer year;

    @PositiveOrZero(message = "Rating must be a positive number.")
    @DecimalMin(value = "0.0", inclusive = true, message = "Rating must be at least 0.")
    @DecimalMax(value = "5.0", inclusive = true, message = "Rating must be at most 5.")
    @NotNull(message = "Rating info cannot be blank.")
    @Schema(description = "Rating info of the new vintage", example = "4.5")
    private Double rating;

    @Schema(description = "Text info of the new review", example = "This wine is amazing!")
    private String text;
}
