package com.wineadvisor.wineadvisor.DTO.wines;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

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
@JsonPropertyOrder({ "wineId", "year", "price", "image" })
public class UpdateVintageDTO {
    @Positive(message = "Wine ID must be a positive number.")
    @NotNull(message = "Wine ID cannot be blank.")
    @Schema(name = "wineId", description = "Wine ID info of the new vintage", example = "1")
    @JsonProperty("wineId")
    private Long wineId;

    @PositiveOrZero(message = "Year info must be a positive number.")
    @NotNull(message = "Year info cannot be blank.")
    @Schema(name = "year", description = "Year info of the new vintage", example = "2000")
    @JsonProperty("year")
    private Integer year;
    
    @Positive(message = "Price info must be a positive number.")
    @NotNull(message = "Price info cannot be blank.")
    @Schema(name = "price", description = "Price info of the new vintage", example = "19.99")
    @JsonProperty("price")
    private Double price;

    @Schema(name = "image", description = "Image info of the new vintage", example = "https://example.com/image.jpg")
    @JsonProperty("image")
    private String image;
}
