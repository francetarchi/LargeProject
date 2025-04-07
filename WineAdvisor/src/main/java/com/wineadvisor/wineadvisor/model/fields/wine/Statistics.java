package com.wineadvisor.wineadvisor.model.fields.wine;

import org.springframework.data.mongodb.core.mapping.Field;

public class Statistics {
    @Field("ratings_count")
    private Long ratingsCount;

    @Field("ratings_average")
    private Double ratingsAverage;
}
