package com.wineadvisor.wineadvisor.controller;

import com.wineadvisor.wineadvisor.service.ReviewService;
import com.wineadvisor.wineadvisor.service.UserService;
import com.wineadvisor.wineadvisor.model.Review;
import com.wineadvisor.wineadvisor.model.User;
import com.wineadvisor.wineadvisor.model.UserId;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.*;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
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

import org.springframework.security.core.context.SecurityContextHolder;


@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<?> addReview(@RequestBody Review review) {
        try {
            review.setId(null);
            review.setLikesCount(0);
            review.setDislikesCount(0);
            review.setCreatedAt(LocalDateTime.now());

            // Prendo lo username dell'utente che ha fatto la richiesta
            String username = ((org.springframework.security.core.userdetails.User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
            User user = null;
            try {
                user = userService.getUserByUsername(username);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utente non trovato."); // 401 Unauthorized
            }

            review.getUserId().setUsername(username);
            review.getUserId().setThumbnail(user.getPicture().getThumbnail());


            Review savedReview = reviewService.addReview(review);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedReview); // 201 Created
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // 400 Bad Request
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // 500 Internal Server Error
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Review> updateReview(@PathVariable Long id, @RequestBody Review updatedReview) {
        return ResponseEntity.ok(reviewService.updateReview(id, updatedReview));
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<Review> addLike(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.addLike(id));
    }

    @PutMapping("/{id}/like")
    public ResponseEntity<Review> removeLike(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.removeLike(id));
    }

    @PostMapping("/{id}/dislike")
    public ResponseEntity<Review> addDislike(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.addDislike(id));
    }

    @PutMapping("/{id}/dislike")
    public ResponseEntity<Review> removeDislike(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.removeDislike(id));
    }

    @GetMapping("/{id}") // Provata: OK
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

    // @GetMapping // Provata: Va gestita con le pagine
    // public ResponseEntity<ArrayList<Review>> getAllReviews() {
    //     return ResponseEntity.ok(reviewService.getAllReviews());
    // }

    @GetMapping("/wine/{wineId}/vintage/{vintageYear}") // Provata: OK
    public ResponseEntity<ArrayList<Review>> getReviewsByVintage(@PathVariable Long wineId, @PathVariable int vintageYear) {
        ArrayList<Review> reviews = new ArrayList<>(reviewService.getReviewsByVintage(wineId, vintageYear));
        if (reviews.isEmpty()) {
            return ResponseEntity.notFound().build(); // Se non ci sono recensioni, restituisco 404
        }
        return ResponseEntity.ok(reviews); // Altrimenti 200 OK con la lista di recensioni
    }

    @GetMapping("/wine/{wineId}") // Provata: OK
    public ResponseEntity<ArrayList<Review>> getReviewsByWine(@PathVariable Long wineId) {
        ArrayList<Review> reviews = new ArrayList<>(reviewService.getReviewsByWine(wineId));
        if (reviews.isEmpty()) {
            return ResponseEntity.notFound().build(); // 404 se non ci sono recensioni per quel vino  
        }
        return ResponseEntity.ok(reviews); // 200 OK con la lista di recensioni
    }

    @GetMapping("/user/{username}") // Provata: OK
    public ResponseEntity<ArrayList<Review>> getReviewsByUser(@PathVariable String username) {
        ArrayList<Review> reviews = new ArrayList<>(reviewService.getReviewsByUser(username));
        if (reviews.isEmpty()) {
            return ResponseEntity.notFound().build(); // 404 se non ci sono recensioni fatte da quell'utente 
        }
        return ResponseEntity.ok(reviews); // 200 OK con la lista di recensioni
    }

    @GetMapping("/user/{username}/wine/{wineId}") // Provata: OK
    public ResponseEntity<ArrayList<Review>> getReviewsByUserAndWine(@PathVariable String username, @PathVariable Long wineId) {
        ArrayList<Review> reviews = new ArrayList<>(reviewService.getReviewsByUserAndWine(username, wineId));
        if (reviews.isEmpty()) {
            return ResponseEntity.notFound().build(); 
        }
        return ResponseEntity.ok(reviews); 
    }

    @GetMapping("/count/wine/{wineId}") // Provata: OK
    public ResponseEntity<Long> getReviewsCountByWine(@PathVariable Long wineId) {
        return ResponseEntity.ok(reviewService.getReviewsCountByWine(wineId));
    }

    @GetMapping("/count/user/{username}") // Provata: OK
    public ResponseEntity<Long> getReviewsCountByUser(@PathVariable String username) {
        return ResponseEntity.ok(reviewService.getReviewsCountByUser(username));
    }

    @GetMapping("/count/wine/{wineId}/vintage/{vintageYear}") // Provata: OK
    public ResponseEntity<Long> getReviewsCountByVintage(@PathVariable Long wineId, @PathVariable int vintageYear) {
        return ResponseEntity.ok(reviewService.getReviewsCountByVintage(wineId, vintageYear));
    }

    // @GetMapping("/sort")
    // public ResponseEntity<ArrayList<Review>> sortReviewsByField(@RequestParam String field, @RequestParam boolean ascendingOrder) {
    //     return ResponseEntity.ok(reviewService.sortReviewsByField(reviewService.getAllReviews(), field, ascendingOrder));
    // }

    @GetMapping("/average/wine/{wineId}/year/{year}") // Provata: OK
    public ResponseEntity<Double> getAverageRatingByWine(@PathVariable Long wineId, @PathVariable int year) {
        return ResponseEntity.ok(reviewService.getAverageRatingByWine(wineId, year));
    }

    @GetMapping("/recent/{num}") // Provata: OK
    public ResponseEntity<ArrayList<Review>> getRecentReviews(@PathVariable int num) {
        ArrayList<Review> reviews = new ArrayList<>(reviewService.getRecentReviews(num));
        if (reviews.isEmpty()) {
            return ResponseEntity.notFound().build(); 
        }
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/recent/user/{username}/{num}") // Provata: OK
    public ResponseEntity<ArrayList<Review>> getRecentReviewsByUser(@PathVariable String username, @PathVariable int num) {
        ArrayList<Review> reviews = new ArrayList<>(reviewService.getRecentReviewsByUser(username, num));
        if (reviews.isEmpty()) {
            return ResponseEntity.notFound().build(); 
        }
        return ResponseEntity.ok(reviews);
    }

    @DeleteMapping("/{id}") // Provata: OK
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        try {
            reviewService.deleteReviewById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/wine/{wineId}") // Provata: OK
    public ResponseEntity<Void> deleteReviewsByWine(@PathVariable Long wineId) {
        try {
            reviewService.deleteReviewsByWine(wineId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/user/{username}") // Provata: OK
    public ResponseEntity<Void> deleteReviewsByUser(@PathVariable String username) {
        try {
            reviewService.deleteReviewsByUser(username);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/wine/{wineId}/vintage/{vintageYear}") // Provata: OK
    public ResponseEntity<Void> deleteReviewsByVintage(@PathVariable Long wineId, @PathVariable int vintageYear) {
        try {
            reviewService.deleteReviewsByVintage(wineId, vintageYear);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/all") // Provata: OK
    public ResponseEntity<Void> deleteAllReviews() {
        try {
            reviewService.deleteAllReviews();
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
