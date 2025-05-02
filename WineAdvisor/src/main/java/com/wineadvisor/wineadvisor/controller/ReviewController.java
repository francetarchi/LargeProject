package com.wineadvisor.wineadvisor.controller;

import com.wineadvisor.wineadvisor.service.ReviewService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import com.wineadvisor.wineadvisor.DTO.reviews.CreateReviewDTO;
import com.wineadvisor.wineadvisor.DTO.reviews.UpdateReviewDTO;
import com.wineadvisor.wineadvisor.exception.ResourceNotFoundException;
import com.wineadvisor.wineadvisor.model.reviews.Review;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import lombok.RequiredArgsConstructor;

import org.apache.coyote.BadRequestException;
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


@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    ////////////// POST //////////////
    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> addReview(@RequestBody @Valid CreateReviewDTO review) {        
        try {
            // Prendo lo username dell'utente che ha fatto la richiesta
            String username = ((org.springframework.security.core.userdetails.User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
            review.setUsername(username);

            Review savedReview = reviewService.addReview(review);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedReview); // 201 Created
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()); // 400 Bad Request
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404 Not Found
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage()); // 500 Internal Server Error
        }
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
        try {
            // Prendo username dell'utente che ha fatto la richiesta
            String username = (String) SecurityContextHolder.getContext().getAuthentication().getName();
            updatedReview.setUsername(username);

            return ResponseEntity.status(HttpStatus.OK).body(reviewService.updateReview(id, updatedReview)); // 200 OK
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    ////////////// GET //////////////
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_WINERY')")
    public ResponseEntity<?> getReviewById(
            @PathVariable
                @NotNull(message = "ID cannot be null.")
                @Positive(message = "ID must be positive.") Long id) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(reviewService.getReviewById(id));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_WINERY')")
    public Page<Review> getAllReviews(Pageable pageable) {
        return reviewService.getAllReviews(pageable);
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
        try {
            return ResponseEntity.status(HttpStatus.OK).body(reviewService.getReviewsByVintage(pageable, wineId, vintageYear));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()); 
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/wines/{wineId}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_WINERY')")
    public ResponseEntity<?> getReviewsByWine(
            @PathVariable
                @NotNull(message = "ID cannot be null.")
                @Positive(message = "ID must be positive.")
                Long wineId,
                Pageable pageable) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(reviewService.getReviewsByWine(pageable, wineId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()); 
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/users/{username}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_WINERY')")
    public ResponseEntity<?> getReviewsByUser(
            @PathVariable @NotBlank(message = "Username cannot be blank.") String username, Pageable pageable) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(reviewService.getReviewsByUser(pageable, username));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()); 
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
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
        try {
            return ResponseEntity.status(HttpStatus.OK).body(reviewService.getReviewsByUserAndWine(pageable, username, wineId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } 
    }

    // @GetMapping("/count")
    // public ResponseEntity<?> getReviewsCount() {
    //     try {
    //         return ResponseEntity.status(HttpStatus.OK).body(reviewService.getReviewsCount());
    //     } catch (Exception e) {
    //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    //     }
    // }

    // @GetMapping("/count/wine/{wineId}")
    // public ResponseEntity<?> getReviewsCountByWine(
    //         @PathVariable
    //             @NotNull(message = "ID cannot be null.")
    //             @Positive(message = "ID must be positive.")
    //             Long wineId) {
    //     try {
    //         return ResponseEntity.status(HttpStatus.OK).body(reviewService.getReviewsCountByWine(wineId));
    //     } catch (ResourceNotFoundException e) {
    //         return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    //     } catch (Exception e) {
    //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    //     }
    // }

    // @GetMapping("/count/user/{username}")
    // public ResponseEntity<?> getReviewsCountByUser(
    //         @PathVariable @NotBlank(message = "Username cannot be blank.") String username) {
    //     try {
    //         return ResponseEntity.status(HttpStatus.OK).body(reviewService.getReviewsCountByUser(username));
    //     } catch (ResourceNotFoundException e) {
    //         return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    //     } catch (Exception e) {
    //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    //     }
    // }

    // @GetMapping("/count/wine/{wineId}/vintage/{vintageYear}")
    // public ResponseEntity<?> getReviewsCountByVintage(
    //         @PathVariable
    //             @NotNull(message = "ID cannot be null.")
    //             @Positive(message = "ID must be positive.")
    //             Long wineId,
    //         @PathVariable
    //             @NotNull(message = "Vintage year cannot be null.")
    //             @PositiveOrZero(message = "Vintage year must be positive.")
    //             Integer vintageYear) {
    //     try {
    //         return ResponseEntity.status(HttpStatus.OK).body(reviewService.getReviewsCountByVintage(wineId, vintageYear));
    //     } catch (ResourceNotFoundException e) {
    //         return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    //     } catch (Exception e) {
    //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    //     }
    // }

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
        try {
            return ResponseEntity.status(HttpStatus.OK).body(reviewService.getAverageRatingByVintage(wineId, year));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
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
        try {
            if (minRating > maxRating) {
                throw new BadRequestException("Minimum rating cannot be greater than maximum rating.");
            }

            return ResponseEntity.status(HttpStatus.OK).body(reviewService.getReviewsByWineAndRatingRange(pageable, wineId, minRating, maxRating));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (BadRequestException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
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
        try {
            if (minRating > maxRating) {
                throw new BadRequestException("Minimum rating cannot be greater than maximum rating.");
            }

            return ResponseEntity.status(HttpStatus.OK).body(reviewService.getReviewsByVintageAndRatingRange(pageable, wineId, vintageYear, minRating, maxRating));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (BadRequestException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
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
        try {
            return ResponseEntity.status(HttpStatus.OK).body(reviewService.getPopularReviewsByVintage(wineId, year, num));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    ////////////// DELETE //////////////
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> deleteReview(
        @PathVariable
            @NotNull(message = "ID cannot be null.")
            @Positive(message = "ID must be positive.")
            Long id) {
        try {
            // Prendo username dell'utente che ha fatto la richiesta
            String username = (String) SecurityContextHolder.getContext().getAuthentication().getName();

            reviewService.deleteReviewById(id, username);
            return ResponseEntity.status(HttpStatus.OK).body("Review successfully deleted.");
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/wines/{wineId}")
    // @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> deleteReviewsByWine(
            @PathVariable
                @NotNull(message = "ID cannot be null.")
                @Positive(message = "ID must be positive.")
                Long wineId) {
        try {
            reviewService.deleteReviewsByWine(wineId);
            return ResponseEntity.status(HttpStatus.OK).body("Reviews successfully deleted.");
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/user/{username}")
    @PreAuthorize("#username == authentication.principal.username or hasRole('ROLE_USER')")
    public ResponseEntity<?> deleteReviewsByUser(@PathVariable @NotBlank(message = "Username cannot be blank.") String username) {
        try {
            reviewService.deleteReviewsByUser(username);
            return ResponseEntity.status(HttpStatus.OK).body("Reviews successfully deleted.");
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
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
        try {
            reviewService.deleteReviewsByVintage(wineId, vintageYear);
            return ResponseEntity.status(HttpStatus.OK).body("Reviews successfully deleted.");
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping
    // @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> deleteAllReviews() {
        try {
            reviewService.deleteAllReviews();
            return ResponseEntity.status(HttpStatus.OK).body("Reviews successfully deleted.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
