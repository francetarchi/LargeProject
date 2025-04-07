package com.wineadvisor.wineadvisor.controller;

import com.wineadvisor.wineadvisor.service.ReviewService;
import com.wineadvisor.wineadvisor.exception.ResourceNotFoundException;
import com.wineadvisor.wineadvisor.model.Review;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;

import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.context.SecurityContextHolder;


@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<?> addReview(@RequestBody Review review) {        
        try {
            // Prendo lo username dell'utente che ha fatto la richiesta
            String username = ((org.springframework.security.core.userdetails.User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
            
            review.getUserId().setUsername(username);

            // Controllo sul testo
            if (review.getText() == null || review.getText().isEmpty()) {
                throw new BadRequestException("Review text cannot be null or empty.");
            }

            // Controllo sul vino
            if (review.getWineId() == null || review.getWineId().getId() == null) {
                throw new BadRequestException("Wine ID cannot be null.");
            }

            // Controlli sul rating
            if (review.getRating() == null) {
                throw new BadRequestException("Rating cannot be null.");
            }
            if (review.getRating() < 0 || review.getRating() > 5) {
                throw new BadRequestException("Rating must be between 0 and 5.");
            }

            Review savedReview = reviewService.addReview(review);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedReview); // 201 Created
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()); // 400 Bad Request
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()); // 400 Bad Request
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404 Not Found
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage()); // 500 Internal Server Error
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateReview(@PathVariable Long id, @RequestBody Review updatedReview) {
        try {   
            // Controllo sull'ID
            if (id == null) {
                throw new BadRequestException("ID cannot be null.");
            }
            if (id <= 0) {
                throw new BadRequestException("ID must be greater than 0.");
            }

            // Prendo username dell'utente che ha fatto la richiesta
            String username = ((org.springframework.security.core.userdetails.User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();

            updatedReview.getUserId().setUsername(username);  

            // Controllo sul testo
            if (updatedReview.getText() == null || updatedReview.getText().isEmpty()) {
                throw new BadRequestException("Review text cannot be null or empty.");
            }

            // Controlli sul rating
            if (updatedReview.getRating() == null) {
                throw new BadRequestException("Rating cannot be null.");
            }
            if (updatedReview.getRating() < 0 || updatedReview.getRating() > 5) {
                throw new BadRequestException("Rating must be between 0 and 5.");
            }

            return ResponseEntity.status(HttpStatus.OK).body(reviewService.updateReview(updatedReview)); // 200 OK
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()); // 400 Bad Request
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()); // 400 Bad Request
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404 Not Found
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage()); // 500 Internal Server Error
        }
    }

    @PutMapping("/{id}/addlike")
    public ResponseEntity<?> addLike(@PathVariable Long id) {
        try {
            // Controllo sull'ID
            if (id == null) {
                throw new BadRequestException("ID cannot be null.");
            }
            if (id <= 0) {
                throw new BadRequestException("ID must be greater than 0.");
            }

            return ResponseEntity.status(HttpStatus.OK).body(reviewService.addLike(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()); 
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/{id}/remlike")
    public ResponseEntity<?> removeLike(@PathVariable Long id) {
        try {
            // Controllo sull'ID
            if (id == null) {
                throw new BadRequestException("ID cannot be null.");
            }
            if (id <= 0) {
                throw new BadRequestException("ID must be greater than 0.");
            }

            return ResponseEntity.status(HttpStatus.OK).body(reviewService.removeLike(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()); 
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/{id}/adddislike")
    public ResponseEntity<?> addDislike(@PathVariable Long id) {
        try {
            // Controllo sull'ID
            if (id == null) {
                throw new BadRequestException("ID cannot be null.");
            }
            if (id <= 0) {
                throw new BadRequestException("ID must be greater than 0.");
            }

            return ResponseEntity.status(HttpStatus.OK).body(reviewService.addDislike(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()); 
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/{id}/remdislike")
    public ResponseEntity<?> removeDislike(@PathVariable Long id) {
        try {
            // Controllo sull'ID
            if (id == null) {
                throw new BadRequestException("ID cannot be null.");
            }
            if (id <= 0) {
                throw new BadRequestException("ID must be greater than 0.");
            }

            return ResponseEntity.status(HttpStatus.OK).body(reviewService.removeDislike(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/{id}") // Provata: OK
    public ResponseEntity<?> getReviewById(@PathVariable Long id) {   
        try {
            // Controllo sull'ID
            if(id == null) {
                throw new BadRequestException("ID cannot be null.");
            }
            if(id <= 0) {
                throw new BadRequestException("ID must be greater than 0.");
            }

            return ResponseEntity.status(HttpStatus.OK).body(reviewService.getReviewById(id));
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()); 
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    // @GetMapping // Provata: Va gestita con le pagine
    // public ResponseEntity<ArrayList<Review>> getAllReviews() {
    //     return ResponseEntity.ok(reviewService.getAllReviews());
    // }

    @GetMapping("/wine/{wineId}/vintage/{vintageYear}") // Provata: OK
    public ResponseEntity<?> getReviewsByVintage(@PathVariable Long wineId, @PathVariable Integer vintageYear) {
        try {
            // Controllo sull'ID del vino
            if (wineId == null) {
                throw new BadRequestException("Wine ID cannot be null.");
            }
            if (wineId <= 0) {
                throw new BadRequestException("Wine ID must be greater than 0.");
            }

            // Controllo sull'anno, che può essere null ma non un numero minore di 0
            if (vintageYear < 0) {
                throw new BadRequestException("Year must be greater than or equal to 0.");
            }

            return ResponseEntity.status(HttpStatus.OK).body(reviewService.getReviewsByVintage(wineId, vintageYear));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()); 
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (BadRequestException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/wine/{wineId}") // Provata: OK
    public ResponseEntity<?> getReviewsByWine(@PathVariable Long wineId) {
        try {
            // Controllo sull'ID del vino
            if (wineId == null) {
                throw new BadRequestException("Wine ID cannot be null.");
            }
            if (wineId <= 0) {
                throw new BadRequestException("Wine ID must be greater than 0.");
            }

            return ResponseEntity.status(HttpStatus.OK).body(reviewService.getReviewsByWine(wineId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()); 
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (BadRequestException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/user/{username}") // Provata: OK
    public ResponseEntity<?> getReviewsByUser(@PathVariable String username) {
        try {
            // Controllo su username
            if (username == null || username.isEmpty()) {
                throw new BadRequestException("Username cannot be null or empty.");
            }

            return ResponseEntity.status(HttpStatus.OK).body(reviewService.getReviewsByUser(username));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()); 
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (BadRequestException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/user/{username}/wine/{wineId}") // Provata: OK
    public ResponseEntity<?> getReviewsByUserAndWine(@PathVariable String username, @PathVariable Long wineId) {
        try {
            // Controllo su username
            if (username == null || username.isEmpty()) {
                throw new BadRequestException("Username cannot be null or empty.");
            }

            // Controllo sull'ID del vino
            if (wineId == null) {
                throw new BadRequestException("Wine ID cannot be null.");
            }
            if (wineId <= 0) {
                throw new BadRequestException("Wine ID must be greater than 0.");
            }

            return ResponseEntity.status(HttpStatus.OK).body(reviewService.getReviewsByUserAndWine(username, wineId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (BadRequestException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } 
    }

    @GetMapping("/count") // Provata: OK
    public ResponseEntity<?> getReviewsCount() {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(reviewService.getReviewsCount());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/count/wine/{wineId}") // Provata: OK
    public ResponseEntity<?> getReviewsCountByWine(@PathVariable Long wineId) {
        try {
            // Controllo sull'ID del vino
            if (wineId == null) {
                throw new BadRequestException("Wine ID cannot be null.");
            }
            if (wineId <= 0) {
                throw new BadRequestException("Wine ID must be greater than 0.");
            }

            return ResponseEntity.status(HttpStatus.OK).body(reviewService.getReviewsCountByWine(wineId));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (BadRequestException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/count/user/{username}") // Provata: OK
    public ResponseEntity<?> getReviewsCountByUser(@PathVariable String username) {
        try {
            // Controllo su username
            if (username == null || username.isEmpty()) {
                throw new BadRequestException("Username cannot be null or empty.");
            }

            return ResponseEntity.status(HttpStatus.OK).body(reviewService.getReviewsCountByUser(username));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (BadRequestException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/count/wine/{wineId}/vintage/{vintageYear}") // Provata: OK
    public ResponseEntity<?> getReviewsCountByVintage(@PathVariable Long wineId, @PathVariable Integer vintageYear) {
        try {
            // Controllo sull'ID del vino
            if (wineId == null) {
                throw new BadRequestException("Wine ID cannot be null.");
            }
            if (wineId <= 0) {
                throw new BadRequestException("Wine ID must be greater than 0.");
            }

            // Controllo sull'anno, che può essere null ma non un numero minore di 0
            if (vintageYear < 0) {
                throw new BadRequestException("Year must be greater than or equal to 0.");
            }

            return ResponseEntity.status(HttpStatus.OK).body(reviewService.getReviewsCountByVintage(wineId, vintageYear));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (BadRequestException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    // @GetMapping("/sort")
    // public ResponseEntity<ArrayList<Review>> sortReviewsByField(@RequestParam String field, @RequestParam boolean ascendingOrder) {
    //     return ResponseEntity.ok(reviewService.sortReviewsByField(reviewService.getAllReviews(), field, ascendingOrder));
    // }

    @GetMapping("/average/wine/{wineId}/year/{year}") // Provata: OK
    public ResponseEntity<?> getAverageRatingByVintage(@PathVariable Long wineId, @PathVariable Integer year) {
        try {
            // Controllo sull'ID del vino
            if (wineId == null) {
                throw new BadRequestException("Wine ID cannot be null.");
            }
            if (wineId <= 0) {
                throw new BadRequestException("Wine ID must be greater than 0.");
            }

            // Controllo sull'anno, che può essere null ma non un numero minore di 0
            if (year < 0) {
                throw new BadRequestException("Year must be greater than or equal to 0.");
            }

            return ResponseEntity.status(HttpStatus.OK).body(reviewService.getAverageRatingByVintage(wineId, year));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (BadRequestException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/recent/{num}") // Provata: OK
    public ResponseEntity<?> getRecentReviews(@PathVariable int num) {
        try {
            // Controllo sul numero di recensioni
            if (num <= 0) {
                throw new BadRequestException("Number of reviews must be greater than 0.");
            }

            return ResponseEntity.status(HttpStatus.OK).body(reviewService.getRecentReviews(num));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (BadRequestException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/recent/user/{username}/{num}") // Provata: OK
    public ResponseEntity<?> getRecentReviewsByUser(@PathVariable String username, @PathVariable int num) {
        try {
            // Controllo su username
            if (username == null || username.isEmpty()) {
                throw new BadRequestException("Username cannot be null or empty.");
            }

            // Controllo sul numero di recensioni
            if (num <= 0) {
                throw new BadRequestException("Number of reviews must be greater than 0.");
            }

            return ResponseEntity.status(HttpStatus.OK).body(reviewService.getRecentReviewsByUser(username, num));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (BadRequestException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/recent/wine/{wineId}/year/{year}/{num}")
    public ResponseEntity<?> getRecentReviewsByVintage(@PathVariable Long wineId, @PathVariable Integer vintageYear, @PathVariable int num) {
        try {
            // Controllo sull'ID del vino
            if (wineId == null) {
                throw new BadRequestException("Wine ID cannot be null.");
            }
            if (wineId <= 0) {
                throw new BadRequestException("Wine ID must be greater than 0.");
            }

            // Controllo sull'anno, che può essere null ma non un numero minore di 0
            if (vintageYear < 0) {
                throw new BadRequestException("Year must be greater than or equal to 0.");
            }

            // Controllo sul numero di recensioni
            if (num <= 0) {
                throw new BadRequestException("Number of reviews must be greater than 0.");
            }

            return ResponseEntity.status(HttpStatus.OK).body(reviewService.getRecentReviewsByVintage(wineId, vintageYear, num));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (BadRequestException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/wine/{wineId}/rating/{minRating}/{maxRating}")
    public ResponseEntity<?> getReviewsByWineAndRating(@PathVariable Long wineId, @PathVariable Double minRating, @PathVariable Double maxRating) {
        try {
            // Controllo sull'ID del vino
            if (wineId == null) {
                throw new BadRequestException("Wine ID cannot be null.");
            }
            if (wineId <= 0) {
                throw new BadRequestException("Wine ID must be greater than 0.");
            }

            // Controllo sul rating minimo e massimo
            if (minRating < 0 || minRating > 5 || maxRating < 0 || maxRating > 5) {
                throw new BadRequestException("Rating must be between 0 and 5.");
            }

            return ResponseEntity.status(HttpStatus.OK).body(reviewService.getReviewsByWineAndRatingRange(wineId, minRating, maxRating));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (BadRequestException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/wine/{wineId}/vintage/{vintageYear}/rating/{minRating}/{maxRating}")
    public ResponseEntity<?> getReviewsByVintageAndRating(@PathVariable Long wineId, @PathVariable Integer vintageYear, @PathVariable Double minRating, @PathVariable Double maxRating) {
        try {
            // Controllo sull'ID del vino
            if (wineId == null) {
                throw new BadRequestException("Wine ID cannot be null.");
            }
            if (wineId <= 0) {
                throw new BadRequestException("Wine ID must be greater than 0.");
            }

            // Controllo sull'anno, che può essere null ma non un numero minore di 0
            if (vintageYear < 0) {
                throw new BadRequestException("Year must be greater than or equal to 0.");
            }

            // Controllo sul rating minimo e massimo
            if (minRating < 0 || minRating > 5 || maxRating < 0 || maxRating > 5) {
                throw new BadRequestException("Rating must be between 0 and 5.");
            }

            return ResponseEntity.status(HttpStatus.OK).body(reviewService.getReviewsByVintageAndRatingRange(wineId, vintageYear, minRating, maxRating));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (BadRequestException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/popular/wine/{wineId}/year/{year}/{num}")
    public ResponseEntity<?> getPopularReviewsByVintage(@PathVariable Long wineId, @PathVariable Integer year, @PathVariable int num) {
        try {
            // Controllo sull'ID del vino
            if (wineId == null) {
                throw new BadRequestException("Wine ID cannot be null.");
            }
            if (wineId <= 0) {
                throw new BadRequestException("Wine ID must be greater than 0.");
            }

            // Controllo sull'anno, che può essere null ma non un numero minore di 0
            if (year < 0) {
                throw new BadRequestException("Year must be greater than or equal to 0.");
            }

            // Controllo sul numero di recensioni
            if (num <= 0) {
                throw new BadRequestException("Number of reviews must be greater than 0.");
            }

            return ResponseEntity.status(HttpStatus.OK).body(reviewService.getPopularReviewsByVintage(wineId, year, num));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (BadRequestException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/")

    @DeleteMapping("/{id}") // Provata: OK
    public ResponseEntity<?> deleteReview(@PathVariable Long id) {
        try {
            // Controllo sull'ID
            if (id == null) {
                throw new BadRequestException("ID cannot be null.");
            }
            if (id <= 0) {
                throw new BadRequestException("ID must be greater than 0.");
            }

            reviewService.deleteReviewById(id);
            return ResponseEntity.noContent().build(); // 204 No Content
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/wine/{wineId}") // Provata: OK
    public ResponseEntity<?> deleteReviewsByWine(@PathVariable Long wineId) {
        try {
            // Controllo sull'ID del vino
            if (wineId == null) {
                throw new BadRequestException("Wine ID cannot be null.");
            }
            if (wineId <= 0) {
                throw new BadRequestException("Wine ID must be greater than 0.");
            }

            reviewService.deleteReviewsByWine(wineId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/user/{username}") // Provata: OK
    public ResponseEntity<?> deleteReviewsByUser(@PathVariable String username) {
        try {
            // Controllo su username
            if (username == null || username.isEmpty()) {
                throw new BadRequestException("Username cannot be null or empty.");
            }

            reviewService.deleteReviewsByUser(username);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/wine/{wineId}/vintage/{vintageYear}") // Provata: OK
    public ResponseEntity<?> deleteReviewsByVintage(@PathVariable Long wineId, @PathVariable Integer vintageYear) {
        try {
            // Controllo sull'ID del vino
            if (wineId == null) {
                throw new BadRequestException("Wine ID cannot be null.");
            }
            if (wineId <= 0) {
                throw new BadRequestException("Wine ID must be greater than 0.");
            }

            // Controllo sull'anno, che può essere null ma non un numero minore di 0
            if (vintageYear < 0) {
                throw new BadRequestException("Year must be greater than or equal to 0.");
            }

            reviewService.deleteReviewsByVintage(wineId, vintageYear);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/all") // Provata: OK
    public ResponseEntity<?> deleteAllReviews() {
        try {
            reviewService.deleteAllReviews();
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
