package com.wineadvisor.wineadvisor.controller;

import com.wineadvisor.wineadvisor.service.ReviewService;
import com.wineadvisor.wineadvisor.DTO.reviews.CreateReviewDTO;
import com.wineadvisor.wineadvisor.DTO.reviews.UpdateReviewDTO;
import com.wineadvisor.wineadvisor.model.reviews.Review;
import com.wineadvisor.wineadvisor.exception.BadRequestException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import lombok.RequiredArgsConstructor;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
    ////////////////////////////////
    /////////// VARIABLES //////////
    ////////////////////////////////
    private final ReviewService reviewService;

    ////////////// POST //////////////
    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> addReview(@RequestBody @Valid CreateReviewDTO review) {        
        // Prendo lo username dell'utente che ha fatto la richiesta
        String username = ((org.springframework.security.core.userdetails.User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        review.setUsername(username);

        Review savedReview = reviewService.addReview(review);
        return ResponseEntity.status(HttpStatus.CREATED).header("Location", "/api/review/" + savedReview.getId()).body(savedReview);
    }

    @PostMapping("/neo4j/{username}")
    public ResponseEntity<Void> createUserReviewsInGraph(
            @PathVariable String username,
            @RequestBody List<Map<String, Object>> reviews) {
        reviewService.createGraphReviewsForUser(username, reviews);
        return ResponseEntity.ok().build();
    }

    ////////////// PUT //////////////
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> updateReview(
            @PathVariable
                @NotNull(message = "ID cannot be null.")
                @Positive(message = "ID must be positive.")
                Long id,
            @RequestBody @Valid UpdateReviewDTO updatedReview) {
        // Prendo username dell'utente che ha fatto la richiesta
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getName();
        updatedReview.setUsername(username);

        return ResponseEntity.status(HttpStatus.OK).body(reviewService.updateReview(id, updatedReview));
    }

    @PutMapping("/neo4j/{username}")
    public ResponseEntity<Void> updateGraphReview(
            @PathVariable String username,
            @RequestBody Map<String, Object> payload) {

        String wineName = (String) payload.get("wineName");
        int wineYear = (int) payload.get("wineYear");
        String newText = (String) payload.get("text");
        double newRating = ((Number) payload.get("rating")).doubleValue();

        reviewService.updateGraphReview(username, wineName, wineYear, newText, newRating);
        return ResponseEntity.ok().build();
    }


    ////////////// GET //////////////
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_WINERY')")
    public ResponseEntity<?> getReviewById(
            @PathVariable
                @NotNull(message = "ID cannot be null.")
                @Positive(message = "ID must be positive.") Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(reviewService.getReviewById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_WINERY')")
    public ResponseEntity<?> getAllReviews(Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(reviewService.getAllReviews(pageable));
    }

    @GetMapping("/wines/{wineId}/vintages/{vintageYear}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_WINERY')")
    public ResponseEntity<?> getReviewsByVintage(
            @PathVariable
                @NotNull(message = "ID cannot be null.")
                @Positive(message = "ID must be positive.")
                Long wineId,
            @PathVariable
                @NotNull(message = "Vintage year cannot be null.")
                @PositiveOrZero(message = "Vintage year must be positive.")
                Integer vintageYear,
            Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(reviewService.getReviewsByVintage(pageable, wineId, vintageYear));
    }

    @GetMapping("/wines/{wineId}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_WINERY')")
    public ResponseEntity<?> getReviewsByWine(
            @PathVariable
                @NotNull(message = "ID cannot be null.")
                @Positive(message = "ID must be positive.")
                Long wineId,
                Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(reviewService.getReviewsByWine(pageable, wineId));
    }

    @GetMapping("/users/{username}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_WINERY')")
    public ResponseEntity<?> getReviewsByUser(
            @PathVariable @NotBlank(message = "Username cannot be blank.") String username, Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(reviewService.getReviewsByUser(pageable, username));
    }

    @GetMapping("/users/{username}/wines/{wineId}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_WINERY')")
    public ResponseEntity<?> getReviewsByUserAndWine(
            @PathVariable 
                @NotBlank(message = "Username cannot be blank.")
                String username,
            @PathVariable
                @NotNull(message = "ID cannot be null.")
                @Positive(message = "ID must be positive.")
                Long wineId,
            Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(reviewService.getReviewsByUserAndWine(pageable, username, wineId));
    }

    @GetMapping("/average/wines/{wineId}/vintages/{year}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_WINERY')")
    public ResponseEntity<?> getAverageRatingByVintage(
            @PathVariable
                @NotNull(message = "ID cannot be null.")
                @Positive(message = "ID must be positive.")
                Long wineId,
            @PathVariable
                @NotNull(message = "Vintage year cannot be null.")
                @PositiveOrZero(message = "Vintage year must be positive.")
                Integer year) {
        return ResponseEntity.status(HttpStatus.OK).body(reviewService.getAverageRatingByVintage(wineId, year));
    }

    @GetMapping("/wine/{wineId}/rating/{minRating}/{maxRating}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_WINERY')")
    public ResponseEntity<?> getReviewsByWineAndRating(
            @PathVariable 
                @NotNull(message = "ID cannot be null.")
                @Positive(message = "ID must be positive.")
                Long wineId,
            @PathVariable 
                @DecimalMin(value = "0.0", inclusive = true, message = "Rating must be at least 0.")
                @DecimalMax(value = "5.0", inclusive = true, message = "Rating must be at most 5.")
                Double minRating,
            @PathVariable
                @DecimalMin(value = "0.0", inclusive = true, message = "Rating must be at least 0.")
                @DecimalMax(value = "5.0", inclusive = true, message = "Rating must be at most 5.")
                Double maxRating,
            Pageable pageable) {
        if (minRating > maxRating) {
            throw new BadRequestException("Minimum rating cannot be greater than maximum rating.");
        }

        return ResponseEntity.status(HttpStatus.OK).body(reviewService.getReviewsByWineAndRatingRange(pageable, wineId, minRating, maxRating));
    }

    @GetMapping("/wines/{wineId}/vintages/{vintageYear}/ratings/{minRating}/{maxRating}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_WINERY')")
    public ResponseEntity<?> getReviewsByVintageAndRating(
            @PathVariable
                @NotNull(message = "ID cannot be null.")
                @Positive(message = "ID must be positive.")
                Long wineId,
            @PathVariable
                @NotNull(message = "Vintage year cannot be null.")
                @PositiveOrZero(message = "Vintage year must be positive.")
                Integer vintageYear,
            @PathVariable 
                @DecimalMin(value = "0.0", inclusive = true, message = "Rating must be at least 0.")
                @DecimalMax(value = "5.0", inclusive = true, message = "Rating must be at most 5.")
                Double minRating,
            @PathVariable
                @DecimalMin(value = "0.0", inclusive = true, message = "Rating must be at least 0.")
                @DecimalMax(value = "5.0", inclusive = true, message = "Rating must be at most 5.")
                Double maxRating,
            Pageable pageable) {
        if (minRating > maxRating) {
            throw new BadRequestException("Minimum rating cannot be greater than maximum rating.");
        }

        return ResponseEntity.status(HttpStatus.OK).body(reviewService.getReviewsByVintageAndRatingRange(pageable, wineId, vintageYear, minRating, maxRating));
    }

    @GetMapping("/wines/{wineId}/vintages/{year}/num/{num}/popular")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_WINERY')")
    public ResponseEntity<?> getPopularReviewsByVintage(
            @PathVariable
                @NotNull(message = "ID cannot be null.")
                @Positive(message = "ID must be positive.")
                Long wineId,
            @PathVariable
                @NotNull(message = "Vintage year cannot be null.")
                @PositiveOrZero(message = "Vintage year must be positive.")
                Integer year,
            @PathVariable
                @NotNull(message = "Number of reviews cannot be null.")
                @Positive(message = "Number of reviews must be positive.")
                int num) {
        return ResponseEntity.status(HttpStatus.OK).body(reviewService.getPopularReviewsByVintage(wineId, year, num));
    }

    @GetMapping("/neo4j/{username}")
    public ResponseEntity<List<Map<String, Object>>> getGraphReviewsByUser(@PathVariable String username) {
        List<Map<String, Object>> reviews = reviewService.getGraphReviewsByUser(username);
        return ResponseEntity.ok(reviews);
    }


    ////////////// DELETE //////////////
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> deleteReview(
        @PathVariable
            @NotNull(message = "ID cannot be null.")
            @Positive(message = "ID must be positive.")
            Long id) {
        // Prendo username dell'utente che ha fatto la richiesta
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getName();

        reviewService.deleteReviewById(id, username);
        return ResponseEntity.status(HttpStatus.OK).body("Review successfully deleted.");
    }
@DeleteMapping("/neo4j")
public ResponseEntity<?> deleteReviewFromGraphAndMongoByUserWineYear(
        @RequestParam String username,
        @RequestParam String wineName,
        @RequestParam Integer wineYear) {

    reviewService.deleteReviewByUsernameAndWine(username, wineName, wineYear);
    return ResponseEntity.ok("Review eliminata da Mongo e Neo4j");
}



    @DeleteMapping("/wines/{wineId}")
    // @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> deleteReviewsByWine(
            @PathVariable
                @NotNull(message = "ID cannot be null.")
                @Positive(message = "ID must be positive.")
                Long wineId) {
        reviewService.deleteReviewsByWine(wineId);
        return ResponseEntity.status(HttpStatus.OK).body("Reviews successfully deleted.");
    }


    @DeleteMapping("/user/{username}")
    @PreAuthorize("#username == authentication.principal.username or hasRole('ROLE_USER')")
    public ResponseEntity<?> deleteReviewsByUser(@PathVariable @NotBlank(message = "Username cannot be blank.") String username) {
        reviewService.deleteReviewsByUser(username);
        return ResponseEntity.status(HttpStatus.OK).body("Reviews successfully deleted.");
    }

    @DeleteMapping("/wines/{wineId}/vintages/{vintageYear}")
    // @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> deleteReviewsByVintage(
            @PathVariable 
                @NotNull(message = "ID cannot be null.")
                @Positive(message = "ID must be positive.")
                Long wineId,
            @PathVariable
                @NotNull(message = "Vintage year cannot be null.")
                @PositiveOrZero(message = "Vintage year must be positive.")
                Integer vintageYear) {
        reviewService.deleteReviewsByVintage(wineId, vintageYear);
        return ResponseEntity.status(HttpStatus.OK).body("Reviews successfully deleted.");
    }

    @DeleteMapping
    // @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> deleteAllReviews() {
        reviewService.deleteAllReviews();
        return ResponseEntity.status(HttpStatus.OK).body("Reviews successfully deleted.");
    }

    @DeleteMapping("/neo4j/{username}")
    public ResponseEntity<Void> deleteUserReviewsFromGraph(@PathVariable String username) {
        reviewService.deleteGraphReviewsForUser(username);
        return ResponseEntity.noContent().build();
    }
}
