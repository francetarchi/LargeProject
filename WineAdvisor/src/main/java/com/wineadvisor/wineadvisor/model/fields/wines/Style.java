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
    private String name;
    private String description;

    @Field("interesting_facts")
    private ArrayList<String> interestingFacts;

    private Integer body;

    private Integer acidity;

    private ArrayList<Food> food;

    private ArrayList<Grape> grapes;

    @Field("baseline_structure")
    private BaselineStructure baselineStructure;
}
