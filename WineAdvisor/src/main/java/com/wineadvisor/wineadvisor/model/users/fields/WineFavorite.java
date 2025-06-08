package com.wineadvisor.wineadvisor.model.users.fields;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class WineFavorite {
    private Long id;
    private String name;
    private String image;
}
