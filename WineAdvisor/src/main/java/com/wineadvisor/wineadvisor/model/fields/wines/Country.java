package com.wineadvisor.wineadvisor.model.fields.wines;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "countries")
public class Country {    
    @Id
    @Field("_id")
    private String id;
    private String name;
    private Currency currency;
}
