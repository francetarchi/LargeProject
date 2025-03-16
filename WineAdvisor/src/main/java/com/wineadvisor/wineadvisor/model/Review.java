package com.wineadvisor.wineadvisor.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import java.util.*;

public class Review {
    @Id
    private ObjectId id;
    private UserInfo user;
    private WineInfo wine;
    private double rating;
    private String text;
    private Date createdAt;
    private int likesCount;
    private int dislikesCount;

    public Review() {}

    public Review(UserInfo user, WineInfo wine, double rating, String text, Date createdAt, int likesCount, int dislikesCount) {
        this.user = user;
        this.wine = wine;
        this.rating = rating;
        this.text = text;
        this.createdAt = createdAt;
        this.likesCount = likesCount;
        this.dislikesCount = dislikesCount;
    }

    public ObjectId getId() {
        return id;
    }

    public UserInfo getUser() {
        return user;
    }

    public WineInfo getWine() {
        return wine;
    }

    public double getRating() {
        return rating;
    }

    public String getText() {
        return text;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public int getLikesCount() {
        return likesCount;
    }

    public int getDislikesCount() {
        return dislikesCount;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }

    public void setWine(WineInfo wine) {
        this.wine = wine;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }

    public void setDislikesCount(int dislikesCount) {
        this.dislikesCount = dislikesCount;
    }

}
