package com.wineadvisor.wineadvisor.model;

import org.bson.types.ObjectId;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document(collection = "reviews")
public class Review {
    @Id
    private ObjectId id;
    private UserId user;
    private WineId wine;
    private double rating;
    private String text;
    private Date createdAt;
    private int likesCount;
    private int dislikesCount;
}
