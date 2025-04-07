package com.wineadvisor.wineadvisor.model.fields.users;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class Location {
    private Street street;

    @Pattern(regexp = "^[a-zA-ZÀ-ÿ'\\-\\s]+$", message = "City can only contain letters, spaces, apostrophes, and hyphens.")
    @Schema(description = "City", example = "Pisa")
    private String city;

    @NotBlank(message = "Region cannot be null.")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ'\\-\\s]+$", message = "Region can only contain letters, spaces, apostrophes, and hyphens.")
    @Schema(description = "Region", example = "Toscana")
    private String region;

    @NotBlank(message = "Country cannot be null.")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ'\\-\\s]+$", message = "Country can only contain letters, spaces, apostrophes, and hyphens.")
    @Schema(description = "Country", example = "Italia")
    private String country;

    @Pattern(regexp = "^[0-9]{4,10}$", message = "Postcode must be between 4 and 10 digits.")
    @Schema(description = "Postcode", example = "56126")
    private String postcode;
}
