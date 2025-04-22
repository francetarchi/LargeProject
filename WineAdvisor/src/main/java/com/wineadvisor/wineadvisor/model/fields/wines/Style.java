package com.wineadvisor.wineadvisor.model.fields.wines;

import java.util.ArrayList;

import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class Style {
    @Field("id")
    private Long id;
    private String name;
    private String description;

    @Field("interesting_facts")
    private String interestingFacts;

    private Integer body;

    @Field("body_description")
    private String bodyDescription;

    private Integer acidity;

    @Field("acidity_description")
    private String acidityDescription;

    private ArrayList<Food> food;

    private ArrayList<Grape> grapes;

    @Field("wines_count")
    private Long winesCount;

    @Field("baseline_structure")
    private BaselineStructure baselineStructure;
}
