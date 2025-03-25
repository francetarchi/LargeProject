package com.wineadvisor.wineadvisor.model;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public abstract class DatePattern {
    @Schema(description = "Date", example = "1970-01-01T00:00:00Z")
    private LocalDateTime date;

    @Schema(description = "Age", example = "55")
    private Integer age;
}
