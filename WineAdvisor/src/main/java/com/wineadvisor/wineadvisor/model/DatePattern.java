package com.wineadvisor.wineadvisor.model;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public abstract class DatePattern {
    @PastOrPresent
    // @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z$", message = "Date must follow the format 'yyyy-MM-ddTHH:mm:ssZ'")
    @Schema(description = "Date", example = "1970-01-01T00:00:00Z")
    private LocalDateTime date;

    @Schema(description = "Age", example = "55")
    private Integer age;
}
