package com.wineadvisor.wineadvisor.model.fields.wine;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Country {    
    private String code;
    private String name;
    private Currency currency;
}
