package com.wineadvisor.wineadvisor.model;

import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;

@Data
public class WineId {
    @Field("id")
    private Long id;
    
    private String name;
    private Integer year;
    private String image;
}
