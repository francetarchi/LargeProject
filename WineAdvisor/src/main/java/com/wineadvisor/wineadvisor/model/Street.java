package com.wineadvisor.wineadvisor.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class Street {
    @Schema(description = "House number", example = "1")
    private String number;

    @Schema(description = "Street name", example = "Via Roma")
    private String name;
}
