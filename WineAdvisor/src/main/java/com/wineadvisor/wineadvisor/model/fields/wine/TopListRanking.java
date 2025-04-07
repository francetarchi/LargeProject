package com.wineadvisor.wineadvisor.model.fields.wine;

import org.springframework.data.mongodb.core.mapping.Field;

public class TopListRanking {
    private Double rank;

    @Field("previous_rank")
    private Double previousRank;

    private String description;

    @Field("top_list")
    private TopList topList;
}
