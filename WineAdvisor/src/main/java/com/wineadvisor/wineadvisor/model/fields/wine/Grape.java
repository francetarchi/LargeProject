package com.wineadvisor.wineadvisor.model.fields.wine;

import org.springframework.data.mongodb.core.mapping.Field;

public class Grape {
    private Long id;
    private String name;

    @Field("wines_count")
    private Long winesCount;
}
