package com.wineadvisor.wineadvisor.model.wines.fields;

import com.wineadvisor.wineadvisor.model.utils.Currency;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CountryEmbedded {
    private String name;
    private Currency currency;
}
