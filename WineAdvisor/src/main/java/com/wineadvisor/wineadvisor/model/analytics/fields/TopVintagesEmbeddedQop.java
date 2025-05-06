package com.wineadvisor.wineadvisor.model.analytics.fields;

import com.wineadvisor.wineadvisor.model.utils.TopVintagesEmbedded;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TopVintagesEmbeddedQop extends TopVintagesEmbedded {
    private Double quality;
    private Double points;
}
