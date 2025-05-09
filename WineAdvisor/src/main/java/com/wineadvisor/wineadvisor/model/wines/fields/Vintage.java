package com.wineadvisor.wineadvisor.model.wines.fields;

import java.util.ArrayList;
import java.time.Instant;

import com.wineadvisor.wineadvisor.model.utils.ReviewEmbedded;

import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class Vintage {
    private Integer year;
    private Double price;
    private Statistics statistics;
    private String image;
    private ArrayList<ReviewEmbedded> reviews;
    
    @Field("created_at")
    private Instant createdAt;
}
