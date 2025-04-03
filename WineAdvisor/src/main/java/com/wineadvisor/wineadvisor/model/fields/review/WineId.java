package com.wineadvisor.wineadvisor.model.fields.review;

import org.springframework.data.mongodb.core.mapping.Field;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class WineId {
    @Field("id")
    private Long id;
    
    @Schema(description = "Wine name", example = "Tignanello")
    private String name;

    @Schema(description = "Year of production", example = "2020")
    private Integer year;

    @Schema(description = "Bottle image", example = "https://example.com/image.jpg")
    private String image;
}
