package com.wineadvisor.wineadvisor.model.wines;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.wineadvisor.wineadvisor.model.wines.fields.Region;
import com.wineadvisor.wineadvisor.model.wines.fields.Statistics;
import com.wineadvisor.wineadvisor.model.wines.fields.Style;
import com.wineadvisor.wineadvisor.model.wines.fields.Taste;
import com.wineadvisor.wineadvisor.model.wines.fields.Vintage;
import com.wineadvisor.wineadvisor.model.wines.fields.WineryEmbedded;

@Data
@Document(collection = "wines")
@JsonInclude(JsonInclude.Include.NON_NULL)
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
