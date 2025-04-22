package com.wineadvisor.wineadvisor.model.fields.wines;

import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Grape {
    @Field("id")
    private Long id;
    private String name;

    @Field("wines_count")
    private Long winesCount;
}
