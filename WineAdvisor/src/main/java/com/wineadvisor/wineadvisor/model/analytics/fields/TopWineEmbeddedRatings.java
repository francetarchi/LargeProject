package com.wineadvisor.wineadvisor.model.analytics.fields;

import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopWineEmbeddedRatings {
    private String wine;
    private String winery;
    private String image;

    @Field("prices_average")
    private Double pricesAverage;

    @Field("ratings_average")
    private Double ratingsAverage;

    @Field("ratings_count")
    private Long ratingsCount;
}
