package com.wineadvisor.wineadvisor.model.analytics;

import lombok.Data;
import lombok.NoArgsConstructor;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "top_wineries_by_wines_ratings")
public class TopWineriesRatings {
    @Id
    private ObjectId _id;

    @Field("winery_username")
    private String wineryUsername;
    private String thumbnail;

    private String winery;

    private String region;
    private String country;

    @Field("ratings_average")
    private Double ratingsAverage;

    @Field("ratings_count")
    private Long ratingsCount;

}
