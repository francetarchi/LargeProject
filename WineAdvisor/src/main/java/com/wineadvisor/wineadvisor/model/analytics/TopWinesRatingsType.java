package com.wineadvisor.wineadvisor.model.analytics;

import java.util.ArrayList;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.wineadvisor.wineadvisor.model.analytics.fields.TopWinesEmbeddedRatings;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "top_wines_by_ratings_per_type")
public class TopWinesRatingsType {
    @Id
    private ObjectId _id;

    private String type;

    private ArrayList<TopWinesEmbeddedRatings> wines;
}
