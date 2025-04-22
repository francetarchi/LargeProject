package com.wineadvisor.wineadvisor.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.wineadvisor.wineadvisor.model.fields.wines.*;

@Data
@Document(collection = "wines")
@AllArgsConstructor
@NoArgsConstructor
public class Wine {
    @Id
    @Field("_id")
    private Long id;

    private String name;

    private String type;

    @Field("is_natural")
    private Boolean isNatural;

    private Region region;

    private WineryEmbedded winery;

    private Taste taste;

    private Style style;
    
    private ArrayList<Vintage> vintages;

    private Statistics statistics;
}
