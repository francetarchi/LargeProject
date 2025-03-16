package com.wineadvisor.wineadvisor.model;

public class WineInfo {
    private int id;
    private String name;
    private int year;
    private String image;

    public WineInfo() {}
    
    public WineInfo(int id, String name, int year, String image) {
        this.id = id;
        this.name = name;
        this.year = year;
        this.image = image;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getYear() {
        return year;
    }

    public String getImage() {
        return image;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
