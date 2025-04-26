package com.wineadvisor.wineadvisor.model.users.fields;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Location {
    private Street street;
    private String city;
    private String region;
    private String country;
    private String postcode;
}
