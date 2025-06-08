package com.wineadvisor.wineadvisor.model.wines.fields;

import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class Statistics {
    @Field("ratings_count")
    private Long ratingsCount;

    @Field("ratings_average")
    private Double ratingsAverage;
}
