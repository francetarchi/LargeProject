package com.wineadvisor.wineadvisor.DTO.ReviewDTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.v3.oas.annotations.media.Schema;
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
@JsonPropertyOrder({ "username", "wineId", "year", "rating", "text" })
public class CreateReviewDTO {
    @NotBlank(message = "Username cannot be null.")
    @Schema(description = "Username of the user who created the review.", example = "silverelephant535")
    @JsonProperty("username")
    private String username;

    @Positive(message = "Wine ID must be a positive number.")
    @NotNull(message = "Wine ID cannot be blank.")
    @Schema(name = "wineId", description = "Wine ID info of the new vintage", example = "1")
    @JsonProperty("wineId")
    private Long wineId;

    @PositiveOrZero(message = "Year must be a positive number.")
    @NotNull(message = "Year info cannot be blank.")
    @Schema(name = "year", description = "Year info of the new vintage", example = "2000")
    @JsonProperty("year")
    private Integer year;

    @PositiveOrZero(message = "Rating must be a positive number.")
    @NotNull(message = "Rating info cannot be blank.")
    @Schema(name = "rating", description = "Rating info of the new vintage", example = "4.5")
    @JsonProperty("rating")
    private Double rating;

    @Schema(name = "text", description = "Text info of the new review", example = "This wine is amazing!")
    @JsonProperty("text")
    private String text;
}
