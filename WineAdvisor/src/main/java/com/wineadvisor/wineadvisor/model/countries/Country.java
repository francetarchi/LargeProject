package com.wineadvisor.wineadvisor.model.countries;

import java.util.ArrayList;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.wineadvisor.wineadvisor.model.wines.fields.Currency;
import com.wineadvisor.wineadvisor.model.utils.VintageEmbedded;

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

    @Field("top_100_vintages_of_the_month")
    private ArrayList<VintageEmbedded> top100VintagesOfTheMonth;
}
