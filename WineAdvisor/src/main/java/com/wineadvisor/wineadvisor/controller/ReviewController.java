package com.wineadvisor.wineadvisor.controller;

import com.wineadvisor.wineadvisor.service.ReviewService;
import com.wineadvisor.wineadvisor.DTO.reviews.CreateReviewDTO;
import com.wineadvisor.wineadvisor.DTO.reviews.UpdateReviewDTO;
import com.wineadvisor.wineadvisor.model.reviews.Review;
import com.wineadvisor.wineadvisor.exception.AccessDeniedException;
import com.wineadvisor.wineadvisor.exception.BadRequestException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import lombok.RequiredArgsConstructor;

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
    @Secured({ "ROLE_USER" })
    public ResponseEntity<?> addReview(@RequestBody @Valid CreateReviewDTO review) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Review savedReview = reviewService.addReview(username, review);
        return ResponseEntity.status(HttpStatus.CREATED).header("Location", "/api/reviews/" + savedReview.getId()).body(savedReview);
    }

    // @PostMapping("/neo4j/{username}")
    // public ResponseEntity<Void> createUserReviewsInGraph(
    //         @PathVariable String username,
    //         @RequestBody List<Map<String, Object>> reviews) {
    //     reviewService.createGraphReviewsForUser(username, reviews);
    //     return ResponseEntity.ok().build();
    // }


    ////////////// GET //////////////
    @GetMapping
    @Secured({ "ROLE_ADMIN" })
    public ResponseEntity<?> getAllReviews(
        @RequestParam(required = false, name = "page number", defaultValue = "0") @PositiveOrZero Integer page) {
        return ResponseEntity.status(HttpStatus.OK).body(reviewService.getAllReviews(page));
    }
    

    @GetMapping("/{id}")
    public ResponseEntity<?> getReviewById(
            @PathVariable
                @NotNull(message = "ID cannot be null.")
                @Positive(message = "ID must be positive.") Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(reviewService.getReviewById(id));
    }

    @GetMapping("/wines/{wineId}/vintages")
    public ResponseEntity<?> getReviewsByVintage(
            @PathVariable
                @NotNull(message = "ID cannot be null.")
                @Positive(message = "ID must be positive.")
                Long wineId,
            @RequestParam(required = false)
                @PositiveOrZero(message = "Vintage year must be positive.")
                Integer vintageYear,
            @RequestParam(required = false, name = "page number", defaultValue = "0") 
                @PositiveOrZero Integer page) {
        return ResponseEntity.status(HttpStatus.OK).body(reviewService.getReviewsByVintage(page, wineId, vintageYear));
    }

    @GetMapping("/wines/{wineId}")
    public ResponseEntity<?> getReviewsByWine(
            @PathVariable
                @NotNull(message = "ID cannot be null.")
                @Positive(message = "ID must be positive.")
                Long wineId,
            @RequestParam(required = false, name = "page number", defaultValue = "0")
                @PositiveOrZero Integer page) {
        return ResponseEntity.status(HttpStatus.OK).body(reviewService.getReviewsByWine(page, wineId));
    }

    @GetMapping("/users/{username}")
    public ResponseEntity<?> getReviewsByUser(
            @PathVariable @NotBlank(message = "Username cannot be blank.") String username,
            @RequestParam(required = false, name = "page number", defaultValue = "0") @PositiveOrZero Integer page) {
        return ResponseEntity.status(HttpStatus.OK).body(reviewService.getReviewsByUser(page, username));
    }

    @GetMapping("/users/{username}/wines/{wineId}")
    public ResponseEntity<?> getReviewsByUserAndWine(
            @PathVariable 
                @NotBlank(message = "Username cannot be blank.")
                String username,
            @PathVariable
                @NotNull(message = "ID cannot be null.")
                @Positive(message = "ID must be positive.")
                Long wineId,
            @RequestParam(required = false, name = "page number", defaultValue = "0")
                @PositiveOrZero Integer page) {
        return ResponseEntity.status(HttpStatus.OK).body(reviewService.getReviewsByUserAndWine(page, username, wineId));
    }

    @GetMapping("/average/wines/{wineId}/vintages/{year}")
    public ResponseEntity<?> getAverageRatingByVintage(
            @PathVariable
                @NotNull(message = "ID cannot be null.")
                @Positive(message = "ID must be positive.")
                Long wineId,
            @PathVariable
                @PositiveOrZero(message = "Vintage year must be positive.")
                Integer year) {
        return ResponseEntity.status(HttpStatus.OK).body(reviewService.getAverageRatingByVintage(wineId, year));
    }

    @GetMapping("/wine/{wineId}/rating/{minRating}/{maxRating}")
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
            @RequestParam(required = false, name = "page number", defaultValue = "0")
                @PositiveOrZero Integer page) {
        if (minRating > maxRating) {
            throw new BadRequestException("Minimum rating cannot be greater than maximum rating.");
        }
        return ResponseEntity.status(HttpStatus.OK).body(reviewService.getReviewsByWineAndRatingRange(page, wineId, minRating, maxRating));
    }

    @GetMapping("/wines/{wineId}/vintages/ratings/")
    public ResponseEntity<?> getReviewsByVintageAndRating(
            @PathVariable
                @NotNull(message = "ID cannot be null.")
                @Positive(message = "ID must be positive.")
                Long wineId,
            @RequestParam(required = false)
                @PositiveOrZero(message = "Vintage year must be positive.")
                Integer vintageYear,
            @RequestParam
                @DecimalMin(value = "0.0", inclusive = true, message = "Rating must be at least 0.")
                @DecimalMax(value = "5.0", inclusive = true, message = "Rating must be at most 5.")
                Double minRating,
            @RequestParam
                @DecimalMin(value = "0.0", inclusive = true, message = "Rating must be at least 0.")
                @DecimalMax(value = "5.0", inclusive = true, message = "Rating must be at most 5.")
                Double maxRating,
            @RequestParam(required = false, name = "page number", defaultValue = "0")
                @PositiveOrZero Integer page) {
        if (minRating > maxRating) {
            throw new BadRequestException("Minimum rating cannot be greater than maximum rating.");
        }

        return ResponseEntity.status(HttpStatus.OK).body(reviewService.getReviewsByVintageAndRatingRange(page, wineId, vintageYear, minRating, maxRating));
    }

    @GetMapping("/wines/{wineId}/vintages/{year}/num/{num}/popular")
    public ResponseEntity<?> getPopularReviewsByVintage(
            @PathVariable
                @NotNull(message = "ID cannot be null.")
                @Positive(message = "ID must be positive.")
                Long wineId,
            @PathVariable
                @PositiveOrZero(message = "Vintage year must be positive.")
                Integer year,
            @PathVariable
                @NotNull(message = "Number of reviews cannot be null.")
                @Positive(message = "Number of reviews must be positive.")
                int num) {
        return ResponseEntity.status(HttpStatus.OK).body(reviewService.getPopularReviewsByVintage(wineId, year, num));
    }
    
    
    ////////////// PUT //////////////
    @PutMapping("/{id}")
    @Secured({ "ROLE_USER" })
    public ResponseEntity<?> updateReview(
                @NotNull(message = "ID cannot be null.") @Positive(message = "ID must be positive.") @PathVariable Long id,
                @Valid @RequestBody UpdateReviewDTO updatedReview) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.status(HttpStatus.OK).body(reviewService.updateReview(id, username, updatedReview));
    }


    ////////////// DELETE //////////////
    @DeleteMapping
    @Secured({ "ROLE_ADMIN" })
    public ResponseEntity<?> deleteAllReviews() {
        reviewService.deleteAllReviews();
        return ResponseEntity.status(HttpStatus.OK).body("Reviews successfully deleted.");
    }

    @DeleteMapping("/{id}")
    @Secured({ "ROLE_ADMIN", "ROLE_USER" })
    public ResponseEntity<?> deleteReviewById(
                @NotNull(message = "ID cannot be null.") @Positive(message = "ID must be positive.") @PathVariable Long id) throws InternalAuthenticationServiceException, AccessDeniedException {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        GrantedAuthority grantedAuthority = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream().findFirst().orElseThrow(
            () -> new InternalAuthenticationServiceException("No role found for authenticated user. Try with another account.")
        );
        
        reviewService.deleteReviewById(id, username, grantedAuthority.getAuthority());
        return ResponseEntity.status(HttpStatus.OK).body("Review successfully deleted.");
    }

    @DeleteMapping("/user/{username}")
    @Secured({ "ROLE_ADMIN", "ROLE_USER" })
    @PreAuthorize("#username == authentication.principal.username or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> deleteReviewsByUser(
        @NotBlank(message = "Username cannot be blank.") @PathVariable String username) {
            reviewService.deleteReviewsByUser(username);
            return ResponseEntity.status(HttpStatus.OK).body("Reviews successfully deleted.");
        }
        
    @DeleteMapping("/wines/{wineId}")
    @Secured({ "ROLE_ADMIN" })
    public ResponseEntity<?> deleteReviewsByWine(
                @NotNull(message = "ID cannot be null.") @Positive(message = "ID must be positive.") @PathVariable Long wineId) {
        reviewService.deleteReviewsByWine(wineId);
        return ResponseEntity.status(HttpStatus.OK).body("Reviews successfully deleted.");
    }

    @DeleteMapping("/wines/{wineId}/vintages")
    @Secured({ "ROLE_ADMIN" })
    public ResponseEntity<?> deleteReviewsByVintage(
                @PathVariable @NotNull(message = "ID cannot be null.") @Positive(message = "ID must be positive.") Long wineId,
                @RequestParam(required = false) @PositiveOrZero(message = "Vintage year must be positive.") Integer vintageYear) {
        reviewService.deleteReviewsByVintage(wineId, vintageYear);
        return ResponseEntity.status(HttpStatus.OK).body("Reviews successfully deleted.");
    }
}
