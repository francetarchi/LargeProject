package com.wineadvisor.wineadvisor.model;

import lombok.Data;

@Data
public class Location {
    private Street street;
    private String city;
    private String region;
    private String country;
    private String postcode;
}
