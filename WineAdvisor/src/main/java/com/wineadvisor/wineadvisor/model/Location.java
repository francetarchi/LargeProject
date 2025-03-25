package com.wineadvisor.wineadvisor.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class Location {
    private Street street;

    @Schema(description = "City", example = "Pisa")
    private String city;

    @Schema(description = "Region", example = "Toscana")
    private String region;

    @Schema(description = "Country", example = "Italia")
    private String country;

    @Schema(description = "Postcode", example = "56126")
    private String postcode;
}
