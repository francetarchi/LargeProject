package com.wineadvisor.wineadvisor.model.analytics;

import java.util.ArrayList;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.wineadvisor.wineadvisor.model.analytics.fields.TopVintagesEmbeddedRatings;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "top_vintages_by_ratings_per_type")
public class TopVintagesRatingsType {
    @Id
    private ObjectId _id;

    private String type;

    private ArrayList<TopVintagesEmbeddedRatings> vintages;
}
