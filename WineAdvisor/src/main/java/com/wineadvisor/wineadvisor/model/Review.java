package com.wineadvisor.wineadvisor.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "reviews")
public class Review {
    @Id
    private Long _id;
    
    private UserInfo user;
    private WineInfo wine;
    private double rating;
    private String text;
    private LocalDateTime createdAt;
    private int likesCount;
    private int dislikesCount;
}
