package com.wineadvisor.wineadvisor.model.utils;

import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VintageEmbedded {
    @Field("wine_id")
    private Long wineId;

    @Field("wine_name")
    private String wineName;

    private Integer year;

    private String bottle;

    private Long count;
}
