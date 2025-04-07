package com.wineadvisor.wineadvisor.model.fields.users;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class Street {
    @Pattern(regexp = "\\d+[a-zA-Z]?", message = "The house number must start with digits and can optionally end with a letter (just one).")
    @Schema(description = "House number", example = "1")
    private String number;

    @Schema(description = "Street name", example = "Via Roma")
    private String name;
}
