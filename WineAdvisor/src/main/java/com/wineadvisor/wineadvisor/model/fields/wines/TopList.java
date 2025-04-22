package com.wineadvisor.wineadvisor.model.fields.wines;

import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopList {
    @Field("id")
    private Long id;
    private String location;
    private String name;
    private String seo_name;
    private Integer type;
    private String year;
}
