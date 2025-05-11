package com.wineadvisor.wineadvisor.model.wines;

import java.util.ArrayList;

import com.wineadvisor.wineadvisor.model.wines.fields.RegionEmbedded;
import com.wineadvisor.wineadvisor.model.wines.fields.Statistics;
import com.wineadvisor.wineadvisor.model.wines.fields.Style;
import com.wineadvisor.wineadvisor.model.wines.fields.Taste;
import com.wineadvisor.wineadvisor.model.wines.fields.Vintage;
import com.wineadvisor.wineadvisor.model.wines.fields.WineryEmbedded;

import com.fasterxml.jackson.annotation.JsonInclude;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "wines")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Wine {
    @Id
    @Field("_id")
    private Long id;

    private String name;

    private String type;

    @Field("is_natural")
    private Boolean isNatural;

    private RegionEmbedded region;

    private WineryEmbedded winery;

    private Taste taste;

    private Style style;
    
    private ArrayList<Vintage> vintages;

    private Statistics statistics;
}
