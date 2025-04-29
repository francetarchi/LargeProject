package com.wineadvisor.wineadvisor.model.wines.fields;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Document(collection = "countries")
@AllArgsConstructor
@NoArgsConstructor
public class Country {    
    @Id
    @Field("_id")
    private String id;
    
    private String name;
    private Currency currency;
}
