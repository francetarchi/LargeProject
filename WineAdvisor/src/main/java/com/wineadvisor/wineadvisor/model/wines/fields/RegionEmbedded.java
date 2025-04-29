package com.wineadvisor.wineadvisor.model.wines.fields;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegionEmbedded {
    private String name;
    private Country country;
}
