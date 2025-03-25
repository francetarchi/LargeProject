package com.wineadvisor.wineadvisor.model;

import lombok.Data;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field; // per rinominare i campi

@Data
@Document(collection = "reviews")
public class Review {
    @Id
    @Field("_id")
    private Long id;

    @Field("user_id")
    private UserId userId;

    @Field("wine_id")
    private WineId wineId;
    
    private double rating;
    private String text;

    @Field("created_at")
    private LocalDateTime createdAt;

    @Field("likes_count")
    private int likesCount;

    @Field("dislikes_count")
    private int dislikesCount;
}
