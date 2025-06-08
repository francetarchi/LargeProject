package com.wineadvisor.wineadvisor.DTO.wines;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewVintageDTO {
    @NotNull(message = "Wine ID cannot be blank.")
    @Positive(message = "Wine ID info cannot be negative.")
    @Schema(description = "Wine ID info of the new vintage", example = "1")
    private Long wineId;

    @NotNull(message = "Year info cannot be null.")
    @PositiveOrZero(message = "Year info cannot be negative.")
    @Schema(description = "Year info of the new vintage", example = "2000")
    private Integer year;

    @NotNull(message = "Price info cannot be blank.")
    @Positive(message = "Price info cannot be negative.")
    @Schema(description = "Price info of the new vintage", example = "19.95")
    private Double price;

    @Schema(description = "Image info of the new vintage", example = "https://example.com/image.jpg")
    private String image;
}
