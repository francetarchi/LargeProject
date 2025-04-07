package com.wineadvisor.wineadvisor.model.fields.wines;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Food {
    private Long id;
    private String name;
    private Double weight;
}
