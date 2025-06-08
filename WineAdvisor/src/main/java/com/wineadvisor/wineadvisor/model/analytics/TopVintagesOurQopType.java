package com.wineadvisor.wineadvisor.model.analytics;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "top_vintages_by_our_qop_per_type")
public class TopVintagesOurQopType extends TopVintagesQopType {
}
