package com.wineadvisor.wineadvisor.model.fields.wine;

import java.lang.reflect.Array;
import java.util.ArrayList;

import org.springframework.data.mongodb.core.mapping.Field;

import com.wineadvisor.wineadvisor.model.fields.ReviewEmbedded;

public class Vintage {
    private Integer year;
    private Double price;
    private Statistics statistics;

    @Field("top_list_rankings")
    private ArrayList<TopListRanking> topListRankings;

    private String image;
    private ArrayList<ReviewEmbedded> reviews;
}
