package com.wineadvisor.wineadvisor.model.fields.wines;

import java.util.ArrayList;

import org.springframework.data.mongodb.core.mapping.Field;

import com.wineadvisor.wineadvisor.model.fields.ReviewEmbedded;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Vintage {
    private Integer year;
    private Double price;
    private Statistics statistics;

    @Field("top_list_rankings")
    private ArrayList<TopListRanking> topListRankings;

    private String image;
    private ArrayList<ReviewEmbedded> reviews;
}
