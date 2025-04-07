package com.wineadvisor.wineadvisor.model.fields;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import com.wineadvisor.wineadvisor.model.fields.review.UserId;
import com.wineadvisor.wineadvisor.model.fields.review.WineId;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewEmbedded {
    @Id
    @Field("_id")
    private Long id;

    @Field("user_id")
    private UserId userId;

    @Field("wine_id")
    private WineId wineId;
    
    @Schema(description = "rating", example = "4.5")
    private Double rating;

    @Schema(description = "text", example = "This wine is amazing!")
    private String text;

    @Field("created_at")
    @Schema(description = "createdAt", example = "2023-10-01T12:00:00Z")
    private LocalDateTime createdAt;

    @Field("likes_count")
    @Schema(description = "likesCount", example = "10")
    private Long likesCount;

    @Field("dislikes_count")
    @Schema(description = "dislikesCount", example = "2")
    private Long dislikesCount;
}
