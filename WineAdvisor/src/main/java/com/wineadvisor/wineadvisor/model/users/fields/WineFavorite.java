package com.wineadvisor.wineadvisor.model.users.fields;


import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WineFavorite {
    @Field("id")
    private Long id;
    
    private String name;
    private String image;
}
