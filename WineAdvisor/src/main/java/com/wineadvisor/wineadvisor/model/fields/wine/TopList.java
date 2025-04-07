package com.wineadvisor.wineadvisor.model.fields.wine;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopList {
    private Long id;
    private String location;
    private String name;
    private String seo_name;
    private Integer type;
    private String year;
}
