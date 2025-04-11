package com.wineadvisor.wineadvisor.model.fields.reviews;


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
    @Field("name")
    private String name;
    @Field("year")
    private Integer year;
    @Field("image")
    private String image;
}
