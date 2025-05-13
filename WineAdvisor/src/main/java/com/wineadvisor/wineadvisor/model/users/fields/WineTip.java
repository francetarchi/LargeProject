package com.wineadvisor.wineadvisor.model.users.fields;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class WineTip {
    private Long id;
    private String name;
    private String image;
    private Integer points;
}
