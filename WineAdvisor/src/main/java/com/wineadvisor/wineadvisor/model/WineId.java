package com.wineadvisor.wineadvisor.model;

import lombok.Data;

@Data
public class WineId {
    private int id;
    private String name;
    private int year;
    private String image;
}
