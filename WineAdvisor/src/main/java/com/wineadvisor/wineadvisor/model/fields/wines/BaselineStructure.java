package com.wineadvisor.wineadvisor.model.fields.wines;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaselineStructure {
    private Double acidity;
    private Double fizziness;
    private Double intensity;
    private Double sweetness;
    private Double tannin;
}
