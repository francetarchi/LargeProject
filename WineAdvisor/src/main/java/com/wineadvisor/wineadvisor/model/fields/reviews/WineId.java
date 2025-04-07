package com.wineadvisor.wineadvisor.model.fields.reviews;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WineId {
    private Long id;    
    private String name;
    private Integer year;
    private String image;
}
