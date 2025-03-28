package com.wineadvisor.wineadvisor.service;

import com.wineadvisor.wineadvisor.repository.ReviewRepository;
import com.wineadvisor.wineadvisor.model.Review;
import com.wineadvisor.wineadvisor.model.User;
import com.wineadvisor.wineadvisor.model.Wine;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.*;

import org.apache.coyote.BadRequestException;
import com.wineadvisor.wineadvisor.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final UserService userService;
    private final WineService wineService;

    // CRUD operations

    // CREATE
    // Aggiunge una recensione alla collection "reviews" del database
    public Review addReview(Review review) {
        User user = userService.getUserByUsername(review.getUserId().getUsername());        

        review.getUserId().setThumbnail(user.getPicture().getThumbnail());

        // Devo controllare che l'utente abbia indicato un vino e un'annata esistenti
        Wine wine = wineService.getWineByVintage(review.getWineId().getId(), review.getWineId().getYear());
        if (wine == null) {
            throw new ResourceNotFoundException("Wine with id " + review.getWineId().getId() + " and year " + review.getWineId().getYear() + " not found.");
        }
        
        // Prendo la image (dell'annata) del vino per la recensione
        String image = null;
        for (int i = 0; i < wine.getVintages().size(); i++) {
            if (wine.getVintages().get(i).getYear() == review.getWineId().getYear()) {
                image = wine.getVintages().get(i).getImage();
                break;
            }
        }
        review.getWineId().setImage(image);
        review.getWineId().setName(wine.getName());
        review.setLikesCount((long) 0);
        review.setDislikesCount((long) 0);
        review.setCreatedAt(LocalDateTime.now());
        
        // Setto l'id della recensione contando il numero di recensioni presenti nel database
        review.setId(getReviewsCount() + 1);

        return reviewRepository.save(review);
    }

    // UPDATE
    // Aggiorna una recensione nella collection "reviews" del db
    public Review updateReview(Review updatedReview) {
        return reviewRepository.findByIdAndUserId_Username(updatedReview.getId(), updatedReview.getUserId().getUsername())
                .map(review -> {
                    if(review.getText() == updatedReview.getText() && review.getRating() == updatedReview.getRating()) { // Controllo che rating e testo siano stati modificati
                        throw new IllegalArgumentException("Rating and text are the same as the previous one.");
                    }
                    review.setRating(updatedReview.getRating());
                    review.setText(updatedReview.getText());
                    return reviewRepository.save(review);
                }).orElseThrow(() -> new ResourceNotFoundException("Review with id " + updatedReview.getId() + " not found."));
    }

    // Aggiunge un like a una recensione
    public Review addLike(Long id) {
        return reviewRepository.findById(id)
            .map(review -> {
                // Controllo che l'utente non abbia già messo like alla recensione
                if (review.getLikedBy().contains(review.getUserId().getUsername())) {
                    throw new IllegalArgumentException("User with username " + review.getUserId().getUsername() + " has already liked this review.");
                }

                // Se l'utente aveva messo in precedenza dislike alla recensione, rimuovo il dislike e tolgo l'utente dalla lista di chi ha messo dislike
                if (review.getDislikedBy().contains(review.getUserId().getUsername())) {
                    review.setDislikesCount(review.getDislikesCount() - 1);
                    review.getDislikedBy().remove(review.getUserId().getUsername());
                }

                // Aggiungo l'utente alla lista di chi ha messo like alla recensione
                review.getLikedBy().add(review.getUserId().getUsername());

                review.setLikesCount(review.getLikesCount() + 1);

                return reviewRepository.save(review);
            }).orElseThrow(() -> new ResourceNotFoundException("Review with id " + id + " not found."));
    }

    // Rimuove un like da una recensione
    public Review removeLike(Long id) {
        return reviewRepository.findById(id)
            .map(review -> {
                // Controllo che l'utente abbia messo like alla recensione
                if (!review.getLikedBy().contains(review.getUserId().getUsername())) {
                    throw new IllegalArgumentException("User with username " + review.getUserId().getUsername() + " has not liked this review.");
                }

                // Rimuovo l'utente dalla lista di chi ha messo like alla recensione
                review.getLikedBy().remove(review.getUserId().getUsername());

                review.setLikesCount(review.getLikesCount() - 1);
                
                return reviewRepository.save(review);
            }).orElseThrow(() -> new ResourceNotFoundException("Review with id " + id + " not found."));
    }

    // Aggiunge un dislike a una recensione
    public Review addDislike(Long id) {
        return reviewRepository.findById(id)
            .map(review -> {
                // Controllo che l'utente non abbia già messo dislike alla recensione
                if (review.getDislikedBy().contains(review.getUserId().getUsername())) {
                    throw new IllegalArgumentException("User with username " + review.getUserId().getUsername() + " has already disliked this review.");
                }

                // Se l'utente aveva messo in precedenza like alla recensione, rimuovo il like e tolgo l'utente dalla lista di chi ha messo like
                if (review.getLikedBy().contains(review.getUserId().getUsername())) {
                    review.setLikesCount(review.getLikesCount() - 1);
                    review.getLikedBy().remove(review.getUserId().getUsername());
                }

                // Aggiungo l'utente alla lista di chi ha messo dislike alla recensione
                review.getDislikedBy().add(review.getUserId().getUsername());

                review.setDislikesCount(review.getDislikesCount() + 1);

                return reviewRepository.save(review);
            }).orElseThrow(() -> new ResourceNotFoundException("Review with id " + id + " not found."));
    }

    // Rimuove un dislike da una recensione
    public Review removeDislike(Long id) {
        return reviewRepository.findById(id)
            .map(review -> {
                // Controllo che l'utente abbia messo dislike alla recensione
                if (!review.getDislikedBy().contains(review.getUserId().getUsername())) {
                    throw new IllegalArgumentException("User with username " + review.getUserId().getUsername() + " has not disliked this review.");
                }

                // Rimuovo l'utente dalla lista di chi ha messo dislike alla recensione
                review.getDislikedBy().remove(review.getUserId().getUsername());

                review.setDislikesCount(review.getDislikesCount() - 1);

                return reviewRepository.save(review);
            }).orElseThrow(() -> new ResourceNotFoundException("Review with id " + id + " not found."));
    }

    // READ
    // Cerca una recensione per id nella collection "reviews" del database
    // Provata: OK
    public Optional<Review> getReviewById(Long id){
        Optional<Review> review = reviewRepository.findById(id);
        if (review.isEmpty()) {
            throw new ResourceNotFoundException("Review with id " + id + " not found.");
        }
        return review;
    }

    // Restituisce tutte le recensioni dalla collection "reviews" del database
    // Provata: Ci vorrebbe la paginazione per gestire il fatto che ci sono tantissime recensioni
    public ArrayList<Review> getAllReviews() {
        return (ArrayList<Review>) reviewRepository.findAll();
    }

    // Restituisce tutte le recensioni di un vino specifico di un'annata specifica dalla collection "reviews" del database
    // Provata: OK
    public ArrayList<Review> getReviewsByVintage(Long wineId, Integer vintageYear) {
        ArrayList<Review> reviews = reviewRepository.findByWineId_IdAndWineId_Year(wineId, vintageYear);
        if (reviews.isEmpty()) {
            throw new ResourceNotFoundException("Reviews for wine with id " + wineId + " and year " + vintageYear + " not found.");
        }
        return reviews;
    }

    // Restituisce tutte le recensioni di un vino specifico dalla collection "reviews" del database
    // Provata: OK
    public ArrayList<Review> getReviewsByWine(Long wineId) {
        ArrayList<Review> reviews = reviewRepository.findByWineId_Id(wineId);
        if (reviews.isEmpty()) {
            throw new ResourceNotFoundException("Reviews for wine with id " + wineId + " not found.");
        }
        return reviews;
    }

    // Restituisce tutte le recensioni di un utente specifico dalla collection "reviews" del database
    // Provata: OK
    public ArrayList<Review> getReviewsByUser(String username) {
        ArrayList<Review> reviews = reviewRepository.findByUserId_Username(username);
        if (reviews.isEmpty()) {
            throw new ResourceNotFoundException("Reviews for user with username " + username + " not found.");
        }
        return reviews;
    }

    // Restituisce tutte le recensioni di un utente specifico per un vino specifico dalla collection "reviews" del database
    // Provata: OK
    public ArrayList<Review> getReviewsByUserAndWine(String username, Long wineId) {
        ArrayList<Review> reviews = reviewRepository.findByUserId_UsernameAndWineId_Id(username, wineId);
        if (reviews.isEmpty()) {
            throw new ResourceNotFoundException("Reviews for user with username " + username + " and wine with id " + wineId + " not found.");
        }
        return reviews;
    }

    // Restituisce il numero di recensioni totali presenti nella collection.
    // Provata: OK
    public Long getReviewsCount() {
        return reviewRepository.count();
    }

    // Restituisce il numero di recensioni fatte per un determinato vino
    // Provata: OK
    public Long getReviewsCountByWine(Long wineId) {
        // Controllo se il vino esiste
        if (wineService.getWineById(wineId) == null) {
            throw new ResourceNotFoundException("Wine with id " + wineId + " not found.");
        }
        return reviewRepository.countByWineId_Id(wineId);
    }

    // Restituisce il numero di recensioni di un determinato utente
    // Provata: OK
    public Long getReviewsCountByUser(String username) {
        // Controllo se l'utente esiste
        if (userService.getUserByUsername(username) == null) {
            throw new ResourceNotFoundException("User with username " + username + " not found.");
        }
        return reviewRepository.countByUserId_Username(username);
    }

    // Restituisce il numero di recensioni di una determinata annata di un vino
    // Provata: OK
    public Long getReviewsCountByVintage(Long wineId, Integer vintageYear) {
        // Controllo se il vino esiste e se l'annata esiste
        if (wineService.getWineById(wineId) == null) {
            throw new ResourceNotFoundException("Wine with id " + wineId + " not found.");
        }
        if (wineService.getWineByVintage(wineId, vintageYear) == null) {
            throw new ResourceNotFoundException("Wine with id " + wineId + " and year " + vintageYear + " not found.");
        }

        return reviewRepository.countByWineId_IdAndWineId_Year(wineId, vintageYear);
    }

    // Ordina e restituisce le recensioni ordinate sulla base del campo specificato, in ordine crescente o decrescente (terzo parametro)
    // Provata: OK
    public ArrayList<Review> sortReviewsByField(ArrayList<Review> reviews, String field, boolean ascendingOrder) {
        reviews.sort((r1, r2) -> {
            switch (field) {
                case "rating":
                    return ascendingOrder ? Double.compare(r1.getRating(), r2.getRating()) : Double.compare(r2.getRating(), r1.getRating());
                case "createdAt":
                    return ascendingOrder ? r1.getCreatedAt().compareTo(r2.getCreatedAt()) : r2.getCreatedAt().compareTo(r1.getCreatedAt());
                case "likesCount":
                    return ascendingOrder ? Long.compare(r1.getLikesCount(), r2.getLikesCount()) : Long.compare(r2.getLikesCount(), r1.getLikesCount());
                case "dislikesCount":
                    return ascendingOrder ? Long.compare(r1.getDislikesCount(), r2.getDislikesCount()) : Long.compare(r2.getDislikesCount(), r1.getDislikesCount());
                case "username":
                    return ascendingOrder ? r1.getUserId().getUsername().compareTo(r2.getUserId().getUsername()) : r2.getUserId().getUsername().compareTo(r1.getUserId().getUsername());
                case "wineId":
                    return ascendingOrder ? r1.getWineId().getId().compareTo(r2.getWineId().getId()) : r2.getWineId().getId().compareTo(r1.getWineId().getId());
                default:
                    return 0;
            }
        });
        return reviews;
                
    }

    // Calcola e restituisce la media dei rating di un'annata di un vino
    // Provata: OK
    public double getAverageRatingByVintage(Long wineId, Integer year){
        // Controllo se il vino e la vintage esistono
        if (wineService.getWineById(wineId) == null) {
            throw new ResourceNotFoundException("Wine with id " + wineId + " not found.");
        }
        if (wineService.getWineByVintage(wineId, year) == null) {
            throw new ResourceNotFoundException("Wine with id " + wineId + " and year " + year + " not found.");
        }

        ArrayList<Review> reviews = reviewRepository.findByWineId_IdAndWineId_Year(wineId, year);
        double sum = 0;
        for (Review review : reviews) {
            sum += review.getRating();
        }
        return sum / reviews.size();
    }

    // Restituisce le num recensioni più recenti
    // Provata: OK
    public ArrayList<Review> getRecentReviews(int num) {
        ArrayList<Review> reviews = (ArrayList<Review>) reviewRepository.findAll();
        // Controllo se reviews è vuoto
        if (reviews.isEmpty()) {
            throw new ResourceNotFoundException("No reviews found.");
        }

        // Ordino le recensioni in base alla data di creazione (createdAt) in ordine decrescente
        ArrayList<Review> sortedReviews = sortReviewsByField(reviews, "createdAt", false);

        // Prendo solo il numero richiesto di recensioni, evitando errori di indice
        int limit = Math.min(num, sortedReviews.size());
        return new ArrayList<>(sortedReviews.subList(0, limit));
    }

    // Restituisce le num recensioni più recenti di un utente specifico
    // Provata: OK
    public ArrayList<Review> getRecentReviewsByUser(String username, int num) {
        // Controllo se l'utente esiste
        if (userService.getUserByUsername(username) == null) {
            throw new ResourceNotFoundException("User with username " + username + " not found.");
        }
        
        ArrayList<Review> reviews = reviewRepository.findByUserId_Username(username);
        // Controllo se reviews è vuoto
        if (reviews.isEmpty()) {
            throw new ResourceNotFoundException("No reviews found for user with username " + username + ".");
        }

        // Ordino le recensioni in base alla data di creazione (createdAt) in ordine decrescente
        ArrayList<Review> sortedReviews = sortReviewsByField(reviews, "createdAt", false);
        
        // Prendo solo il numero richiesto di recensioni, evitando errori di indice
        int limit = Math.min(num, sortedReviews.size());
        return new ArrayList<>(sortedReviews.subList(0, limit));
    }

    // Restituisce le num recensioni più recenti di un'annata specifica per un determinato vino
    // Provata: OK
    public ArrayList<Review> getRecentReviewsByVintage(Long wineId, Integer vintageYear, int num) {
        // Controllo se il vino e la vintage esistono
        if (wineService.getWineById(wineId) == null) {
            throw new ResourceNotFoundException("Wine with id " + wineId + " not found.");
        }
        if (wineService.getWineByVintage(wineId, vintageYear) == null) {
            throw new ResourceNotFoundException("Wine with id " + wineId + " and year " + vintageYear + " not found.");
        }


        ArrayList<Review> reviews = reviewRepository.findByWineId_IdAndWineId_Year(wineId, vintageYear);
        // Controllo se reviews è vuoto
        if (reviews.isEmpty()) {
            throw new ResourceNotFoundException("No reviews found for wine with id " + wineId + " and year " + vintageYear + ".");
        }

        // Ordino le recensioni in base alla data di creazione (createdAt) in ordine decrescente
        ArrayList<Review> recentReviews = sortReviewsByField(reviews, "createdAt", false);
        
        // Prendo solo il numero richiesto di recensioni, evitando errori di indice
        int limit = Math.min(num, recentReviews.size()); 
        return new ArrayList<>(recentReviews.subList(0, limit));
    }

    // Restituisce le recensioni di un vino specifico in un range di rating specifico
    public ArrayList<Review> getReviewsByWineAndRatingRange(Long wineId, Double minRating, Double maxRating) {
        // Controllo se il vino esiste
        if (wineService.getWineById(wineId) == null) {
            throw new ResourceNotFoundException("Wine with id " + wineId + " not found.");
        }
        ArrayList<Review> reviews =  reviewRepository.findByWineId_IdAndRatingBetween(wineId, minRating, maxRating);

        // Controllo se reviews è vuoto
        if (reviews.isEmpty()) {
            throw new ResourceNotFoundException("No reviews found for wine with id " + wineId + " in the rating range [" + minRating + ", " + maxRating + "].");
        }
        return reviews;
    }

    // Restituisce le recensioni di un'annata specifica per un determinato vino in un range di rating specifico
    public ArrayList<Review> getReviewsByVintageAndRatingRange(Long wineId, Integer year, Double minRating, Double maxRating) {
        // Controllo se il vino e la vintage esistono
        if (wineService.getWineById(wineId) == null) {
            throw new ResourceNotFoundException("Wine with id " + wineId + " not found.");
        }
        if (wineService.getWineByVintage(wineId, year) == null) {
            throw new ResourceNotFoundException("Wine with id " + wineId + " and year " + year + " not found.");
        }

        ArrayList<Review> reviews = reviewRepository.findByWineId_IdAndWineId_YearAndRatingBetween(wineId, year, minRating, maxRating);
        // Controllo se reviews è vuoto
        if (reviews.isEmpty()) {
            throw new ResourceNotFoundException("No reviews found for wine with id " + wineId + " and year " + year + " in the rating range [" + minRating + ", " + maxRating + "].");
        }
        return reviews;
    }

    // Restituisce le num recensioni più popolari (con più like) di un'annata specifica per un determinato vino
    public ArrayList<Review> getPopularReviewsByVintage(Long wineId, Integer vintageYear, int num) {
        // Controllo se il vino e la vintage esistono
        if (wineService.getWineById(wineId) == null) {
            throw new ResourceNotFoundException("Wine with id " + wineId + " not found.");
        }
        if (wineService.getWineByVintage(wineId, vintageYear) == null) {
            throw new ResourceNotFoundException("Wine with id " + wineId + " and year " + vintageYear + " not found.");
        }

        ArrayList<Review> reviews = reviewRepository.findByWineId_IdAndWineId_Year(wineId, vintageYear);

        // Controllo se reviews è vuoto
        if (reviews.isEmpty()) {
            throw new ResourceNotFoundException("No reviews found for wine with id " + wineId + " and year " + vintageYear + ".");
        }

        // Ordino le recensioni in base al numero di like (likesCount) in ordine decrescente
        ArrayList<Review> popularReviews = sortReviewsByField(reviews, "likesCount", false);
        
        // Prendo solo il numero richiesto di recensioni, evitando errori di indice
        int limit = Math.min(num, popularReviews.size()); 
        return new ArrayList<>(popularReviews.subList(0, limit));
    }

    // DELETE
    // Cancella una recensione specifica
    // Provata: OK
    public void deleteReviewById(Long id) {
        // Controllo se la recensione esiste
        if (reviewRepository.findById(id).isEmpty()) {
            throw new ResourceNotFoundException("Review with id " + id + " not found.");
        }

        reviewRepository.deleteById(id);
    }

    // Cancella tutte le recensioni di un vino specifico
    // Provata: OK
    public void deleteReviewsByWine(Long wineId) {
        // Controllo se il vino esiste
        if (wineService.getWineById(wineId) == null) {
            throw new ResourceNotFoundException("Wine with id " + wineId + " not found.");
        }

        reviewRepository.deleteByWineId_Id(wineId);
    }

    // Cancella tutte le recensioni di un utente specifico
    // Provata: OK
    public void deleteReviewsByUser(String username) {
        // Controllo se l'utente esiste
        if (userService.getUserByUsername(username) == null) {
            throw new ResourceNotFoundException("User with username " + username + " not found.");
        }

        reviewRepository.deleteByUserId_Username(username);
    }

    // Cancella tutte le recensioni di un'annata specifica di un vino specifico
    // Provata: OK
    public void deleteReviewsByVintage(Long wineId, Integer vintageYear) {
        // Controllo se il vino e la vintage esistono
        if (wineService.getWineById(wineId) == null) {
            throw new ResourceNotFoundException("Wine with id " + wineId + " not found.");
        }
        if (wineService.getWineByVintage(wineId, vintageYear) == null) {
            throw new ResourceNotFoundException("Wine with id " + wineId + " and year " + vintageYear + " not found.");
        }
        
        reviewRepository.deleteByWineId_IdAndWineId_Year(wineId, vintageYear);
    }

    // Cancella tutte le recensioni
    // Provata: OK
    public void deleteAllReviews() {
        reviewRepository.deleteAll();
    }
}
