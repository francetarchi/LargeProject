package com.wineadvisor.wineadvisor.model.fields.users;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Picture {
    private String large;
    private String medium;
    private String thumbnail;
}
