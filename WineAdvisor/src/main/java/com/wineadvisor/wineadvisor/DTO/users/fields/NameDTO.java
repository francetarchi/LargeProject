package com.wineadvisor.wineadvisor.DTO.users.fields;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.wineadvisor.wineadvisor.model.fields.users.Name;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@JsonPropertyOrder({ "title", "first", "last" })
public class NameDTO extends Name {
    @Pattern(regexp = "Mr\\.|Miss\\.|", message = "Title must be one between \"Mr.\" and \"Miss.\" (or blank).")
    @Schema(description = "Title", example = "Mr.")
    private String title;

    @NotBlank(message = "First name cannot be blank.")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ'\\-\\s]+$", message = "First name must contain only letters, spaces, hyphens, and apostrophes.")
    @Schema(name = "first name", description = "First name", example = "Mario")
    @JsonProperty("first name")
    private String first;

    @NotBlank(message = "Last name cannot be blank.")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ'\\-\\s]+$", message = "Last name must contain only letters, spaces, hyphens, and apostrophes.")
    @Schema(name = "last name", description = "Last name (surname)", example = "Rossi")
    @JsonProperty("last name")
    private String last;



    ///////////// METODI PUBBLICI /////////////
    public Name toName() {
        return new Name(this.getTitle(), this.getFirst(), this.getLast());
    }
}
