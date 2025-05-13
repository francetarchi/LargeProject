package com.wineadvisor.wineadvisor.model.styles;

import java.util.ArrayList;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.wineadvisor.wineadvisor.model.utils.Food;
import com.wineadvisor.wineadvisor.model.utils.Grape;
import com.wineadvisor.wineadvisor.model.utils.Taste;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "styles")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Style {
    @Id
    @Field("_id")
    private ObjectId id;
    private String name;
    private String description;

    @Field("interesting_facts")
    private ArrayList<String> interestingFacts;

    private ArrayList<Food> food;

    private ArrayList<Grape> grapes;

    private Taste taste;
}
