package com.wineadvisor.wineadvisor.DTO.ReviewDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateReviewDTO {
    private String username;
    private Long wineId;
    private Integer year;
    private Double rating;
    private String text;
}
