package com.wineadvisor.wineadvisor.controller;

import com.wineadvisor.wineadvisor.service.ReviewService;
import org.bson.Document;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    public ReviewController(ReviewService revService) {
        this.reviewService = revService;
    }

}
