package com.wineadvisor.wineadvisor.model;

import lombok.Data;

@Data
public class WineInfo {
    private Long id;
    private String name;
    private int year;
    private String image;
}
