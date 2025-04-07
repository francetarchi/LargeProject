package com.wineadvisor.wineadvisor.model.fields.wine;

import org.springframework.data.mongodb.core.mapping.Field;

public class Structure {
    private Double acidity;
    private Double fiziness;
    private Double intensity;
    private Double sweetness;
    private Double tannin;

    @Field("user_structure_count")
    private Long userStructureCount;

    @Field("calculated_structure_count")
    private Long calculatedStructureCount;
}
