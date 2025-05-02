package com.wineadvisor.wineadvisor.model.countries;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.wineadvisor.wineadvisor.model.wines.fields.Currency;

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
    private ObjectId id; 
    private String name;
    private Currency currency;
}
