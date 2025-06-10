package com.wineadvisor.wineadvisor.model.utils;

import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopVintageEmbedded {
    private String wine;
    private String winery;
    private Integer year;
    private Double price;
    private String image;

    @Field("ratings_count")
    private Long ratingsCount;
}
