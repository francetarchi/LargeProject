package com.wineadvisor.wineadvisor.model.wines.fields;

import java.util.ArrayList;

import org.springframework.data.mongodb.core.mapping.Field;

import com.wineadvisor.wineadvisor.model.utils.Food;
import com.wineadvisor.wineadvisor.model.utils.Grape;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class StyleEmbedded {
    private String name;
    private String description;

    @Field("interesting_facts")
    private ArrayList<String> interestingFacts;

    private ArrayList<Food> food;

    private ArrayList<Grape> grapes;
}
