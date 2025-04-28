package com.wineadvisor.wineadvisor.DTO.wines;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewGrapeDTO {
    @NotBlank(message = "Name is mandatory.")
    @Schema(description = "Name of the grape", example = "Sangiovese")
    private String name;
}
