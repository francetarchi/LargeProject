package com.wineadvisor.wineadvisor.DTO.ReviewDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateReviewDTO {
    private String username;
    private Double rating;
    private String text;
}
