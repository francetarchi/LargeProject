package com.wineadvisor.wineadvisor.model.users.fields;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Street {
    private String number;
    private String name;
}