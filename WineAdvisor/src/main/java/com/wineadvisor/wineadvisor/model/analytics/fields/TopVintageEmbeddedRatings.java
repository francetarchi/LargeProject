package com.wineadvisor.wineadvisor.model.analytics.fields;

import org.springframework.data.mongodb.core.mapping.Field;

import com.wineadvisor.wineadvisor.model.utils.TopVintageEmbedded;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TopVintageEmbeddedRatings extends TopVintageEmbedded {
    @Field("ratings_average")
    private Double ratingsAverage;
}
