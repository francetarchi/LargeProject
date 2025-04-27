package com.wineadvisor.wineadvisor.model.wines.fields;

import java.util.ArrayList;

import com.wineadvisor.wineadvisor.model.utils.ReviewEmbedded;

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
}
