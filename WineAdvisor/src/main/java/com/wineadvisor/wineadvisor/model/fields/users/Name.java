package com.wineadvisor.wineadvisor.model.fields.users;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class Name {
    @Pattern(regexp = "Mr\\.|Miss\\.|", message = "Title must be one between \"Mr.\" and \"Miss.\" (or blank).")
    @Schema(description = "Title", example = "Mr.")
    private String title;

    @NotBlank(message = "First name cannot be blank.")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ'\\-\\s]+$", message = "First name must contain only letters, spaces, hyphens, and apostrophes.")
    @Schema(description = "First name", example = "Mario")
    private String first;

    @NotBlank(message = "Last name cannot be blank.")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ'\\-\\s]+$", message = "Last name must contain only letters, spaces, hyphens, and apostrophes.")
    @Schema(description = "Last name", example = "Rossi")
    private String last;
}
