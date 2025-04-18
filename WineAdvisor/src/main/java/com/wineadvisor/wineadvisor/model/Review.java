package com.wineadvisor.wineadvisor.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.wineadvisor.wineadvisor.model.fields.reviews.UserId;
import com.wineadvisor.wineadvisor.model.fields.reviews.WineId;

@Data
@Document(collection = "reviews")
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
public class Review {
    @Id
    @Field("_id")
    private Long id;

    @Field("user_id")
    private UserId userId;

    @Field("wine_id")
    private WineId wineId;
    
    private Double rating;

    private String text;

    @Field("created_at")
    private LocalDateTime createdAt;

    @Field("likes_count")
    private Long likesCount;

    @Field("dislikes_count")
    private Long dislikesCount;
}
