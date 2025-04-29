package com.wineadvisor.wineadvisor.model.regions;

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
    @Field("_id")
    private Long id;
    private String name;
    private String country;
}
