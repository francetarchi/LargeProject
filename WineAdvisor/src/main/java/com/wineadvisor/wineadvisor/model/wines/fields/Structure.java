package com.wineadvisor.wineadvisor.model.wines.fields;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class Structure {
    private Double acidity;
    private Double fizziness;
    private Double intensity;
    private Double sweetness;
    private Double tannin;
}
