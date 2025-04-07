package com.wineadvisor.wineadvisor.model.fields.wines;

import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopListRanking {
    private Double rank;

    @Field("previous_rank")
    private Double previousRank;

    private String description;

    @Field("top_list")
    private TopList topList;
}
