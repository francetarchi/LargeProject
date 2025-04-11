package com.wineadvisor.wineadvisor.model.fields.reviews;


import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserId {
    @Field("username")
    private String username;
    @Field("thumbnail")
    private String thumbnail;
}
