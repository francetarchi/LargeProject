package com.wineadvisor.wineadvisor.model.reviews;

import java.time.Instant;

import com.wineadvisor.wineadvisor.model.reviews.fields.UserId;
import com.wineadvisor.wineadvisor.model.reviews.fields.WineId;

import com.fasterxml.jackson.annotation.JsonInclude;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


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
    private Instant createdAt;

    @Field("likes_count")
    private Long likesCount;

    @Field("dislikes_count")
    private Long dislikesCount;
}
