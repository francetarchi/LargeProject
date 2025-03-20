package com.wineadvisor.wineadvisor.controller;

import com.wineadvisor.wineadvisor.service.ReviewService;
import com.wineadvisor.wineadvisor.model.Review;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;

import java.util.*;

// import org.bson.types.ObjectId;
// import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<Review> addReview(@RequestBody Review review) {
        return ResponseEntity.ok(reviewService.addReview(review));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Review> updateReview(@PathVariable Long id, @RequestBody Review updatedReview) {
        return ResponseEntity.ok(reviewService.updateReview(id, updatedReview));
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<Review> addLike(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.addLike(id));
    }

    @DeleteMapping("/{id}/like")
    public ResponseEntity<Review> removeLike(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.removeLike(id));
    }

    @PostMapping("/{id}/dislike")
    public ResponseEntity<Review> addDislike(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.addDislike(id));
    }

    @DeleteMapping("/{id}/dislike")
    public ResponseEntity<Review> removeDislike(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.removeDislike(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Review> getReviewById(
        @PathVariable 
        @Parameter(description = "ID della recensione", example = "1") 
        Long id) {     
            try {
                return reviewService.getReviewById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build(); // Se l'ID non è valido, restituisce 400
            }
    }

    // @GetMapping
    // public ResponseEntity<ArrayList<Review>> getAllReviews() {
    //     return ResponseEntity.ok(reviewService.getAllReviews());
    // }

    @GetMapping("/wine/{wineId}/vintage/{vintageYear}")
    public ResponseEntity<ArrayList<Review>> getReviewsByVintage(@PathVariable Long wineId, @PathVariable int vintageYear) {
        return ResponseEntity.ok(reviewService.getReviewsByVintage(wineId, vintageYear));
    }

    @GetMapping("/wine/{wineId}")
    public ResponseEntity<ArrayList<Review>> getReviewsByWine(@PathVariable Long wineId) {
        return ResponseEntity.ok(reviewService.getReviewsByWine(wineId));
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<ArrayList<Review>> getReviewsByUser(@PathVariable String username) {
        return ResponseEntity.ok(reviewService.getReviewsByUser(username));
    }

    @GetMapping("/user/{username}/wine/{wineId}")
    public ResponseEntity<ArrayList<Review>> getReviewsByUserAndWine(@PathVariable String username, @PathVariable Long wineId) {
        return ResponseEntity.ok(reviewService.getReviewsByUserAndWine(username, wineId));
    }

    @GetMapping("/count/wine/{wineId}")
    public ResponseEntity<Long> getReviewsCountByWine(@PathVariable Long wineId) {
        return ResponseEntity.ok(reviewService.getReviewsCountByWine(wineId));
    }

    @GetMapping("/count/user/{username}")
    public ResponseEntity<Long> getReviewsCountByUser(@PathVariable String username) {
        return ResponseEntity.ok(reviewService.getReviewsCountByUser(username));
    }

    @GetMapping("/count/wine/{wineId}/vintage/{vintageYear}")
    public ResponseEntity<Long> getReviewsCountByVintage(@PathVariable Long wineId, @PathVariable int vintageYear) {
        return ResponseEntity.ok(reviewService.getReviewsCountByVintage(wineId, vintageYear));
    }

    @GetMapping("/sort")
    public ResponseEntity<ArrayList<Review>> sortReviewsByField(@RequestParam String field, @RequestParam boolean ascendingOrder) {
        return ResponseEntity.ok(reviewService.sortReviewsByField(reviewService.getAllReviews(), field, ascendingOrder));
    }

    @GetMapping("/average/wine/{wineId}/year/{year}")
    public ResponseEntity<Double> getAverageRatingByWine(@PathVariable Long wineId, @PathVariable int year) {
        return ResponseEntity.ok(reviewService.getAverageRatingByWine(wineId, year));
    }

    @GetMapping("/recent/{num}")
    public ResponseEntity<ArrayList<Review>> getRecentReviews(@PathVariable int num) {
        return ResponseEntity.ok(reviewService.getRecentReviews(num));
    }

    @GetMapping("/recent/user/{username}/{num}")
    public ResponseEntity<ArrayList<Review>> getRecentReviewsByUser(@PathVariable String username, @PathVariable int num) {
        return ResponseEntity.ok(reviewService.getRecentReviewsByUser(username, num));
    }

    @DeleteMapping("/all")
    public ResponseEntity<Void> deleteAllReviews() {
        reviewService.deleteAllReviews();
        return ResponseEntity.noContent().build();
    }
}
