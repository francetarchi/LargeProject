package com.wineadvisor.wineadvisor.DTO.WineDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewVintageDTO {
    private Long wineId;
    private Integer year;
    private Double price;
    private String image;
}
