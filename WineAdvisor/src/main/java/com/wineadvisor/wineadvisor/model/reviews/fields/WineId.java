package com.wineadvisor.wineadvisor.model.reviews.fields;


import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WineId {
    @Field("id")
    private Long id;
    private String name;
    private Integer year;
    private String image;
}
