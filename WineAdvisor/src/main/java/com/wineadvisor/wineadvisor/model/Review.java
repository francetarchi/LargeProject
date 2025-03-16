package com.wineadvisor.wineadvisor.model;

import org.bson.types.ObjectId;
import lombok.Data;

import java.util.*;

@Data
public class Review {
    private ObjectId id;
    private UserInfo user;
    private WineInfo wine;
    private double rating;
    private String text;
    private Date createdAt;
    private int likesCount;
    private int dislikesCount;
}
