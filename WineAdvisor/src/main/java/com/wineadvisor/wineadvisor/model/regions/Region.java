package com.wineadvisor.wineadvisor.model.regions;

import java.util.ArrayList;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.wineadvisor.wineadvisor.model.utils.VintageEmbedded;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Document(collection = "regions")
@AllArgsConstructor
@NoArgsConstructor
public class Region {
    @Id
    @Field("_id")
    private ObjectId id;
    private String name;
    private String country;

    @Field("top_10_vintages_of_the_month")
    private ArrayList<VintageEmbedded> top10VintagesOfTheMonth;
}
