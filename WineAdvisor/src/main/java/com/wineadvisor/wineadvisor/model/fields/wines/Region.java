package com.wineadvisor.wineadvisor.model.fields.wines;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Region {
    private String name;
    private Country country;
}
