package com.wineadvisor.wineadvisor.model;



import lombok.Data;

@Data
public class WineId {
    private Long id;
    private String name;
    private int year;
    private String image;
}
