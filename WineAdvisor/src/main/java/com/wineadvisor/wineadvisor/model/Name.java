package com.wineadvisor.wineadvisor.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class Name {
    @Schema(description = "Title", example = "Mr")
    private String title;

    @Schema(description = "First name", example = "Mario")
    private String first;

    @Schema(description = "Last name", example = "Rossi")
    private String last;
}
