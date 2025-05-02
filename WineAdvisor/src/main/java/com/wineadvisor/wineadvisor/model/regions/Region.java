package com.wineadvisor.wineadvisor.model.regions;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

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
}
