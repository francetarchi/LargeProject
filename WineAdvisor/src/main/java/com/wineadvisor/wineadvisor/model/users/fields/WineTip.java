package com.wineadvisor.wineadvisor.model.users.fields;

import java.time.Instant;

import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class WineTip {
    @Field("wine_id")
    private Long wineId;

    @Field("wine_name")
    private String wineName;
    
    @Field("wine_type")
    private String wineType;

    @Field("wine_style")
    private String wineStyle;

    @Field("wine_rating")
    private Double wineRating;

    @Field("wine_ratings_count")
    private Long wineRatingsCount;

    @Field("wine_price_avg")
    private Double winePriceAvg;

    @Field("wine_image")
    private String wineImage;

    @Field("wine_created_at")
    private Instant wineCreatedAt;
}
