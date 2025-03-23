package com.wineadvisor.wineadvisor.service;

import com.wineadvisor.wineadvisor.repository.ReviewRepository;
import com.wineadvisor.wineadvisor.model.Review;

import lombok.RequiredArgsConstructor;

import java.util.*;

// import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;

    // CRUD operations
    // Funzioni per l'utente
    // Aggiunge una recensione alla collection "reviews" del database
    public Review addReview(Review review) {
        return reviewRepository.save(review);
    }

    // Rimuove una recensione dalla collection "reviews" del database
    public void deleteReview(Long id) {
        reviewRepository.deleteById(id);        
    }

    // Aggiorna una recensione nella collection "reviews" del db
    public Review updateReview(Long id, Review updatedReview) {
        return reviewRepository.findById(id)
                .map(review -> {
                    review.setUserId(updatedReview.getUserId());
                    review.setWineId(updatedReview.getWineId());
                    review.setRating(updatedReview.getRating());
                    review.setText(updatedReview.getText());
                    review.setCreatedAt(updatedReview.getCreatedAt());
                    review.setLikesCount(updatedReview.getLikesCount());
                    review.setDislikesCount(updatedReview.getDislikesCount());
                    return reviewRepository.save(review);
                }).orElseThrow(() -> new RuntimeException("Review not found"));
    }

    // Aggiunge un like a una recensione
    public Review addLike(Long id) {
        return reviewRepository.findById(id)
            .map(review -> {
                review.setLikesCount(review.getLikesCount() + 1);
                return reviewRepository.save(review);
            }).orElseThrow(() -> new RuntimeException("Review not found"));
    }

    // Rimuove un like da una recensione
    public Review removeLike(Long id) {
        return reviewRepository.findById(id)
            .map(review -> {
                review.setLikesCount(review.getLikesCount() - 1);
                return reviewRepository.save(review);
            }).orElseThrow(() -> new RuntimeException("Review not found"));
    }

    // Aggiunge un dislike a una recensione
    public Review addDislike(Long id) {
        return reviewRepository.findById(id)
            .map(review -> {
                review.setDislikesCount(review.getDislikesCount() + 1);
                return reviewRepository.save(review);
            }).orElseThrow(() -> new RuntimeException("Review not found"));
    }

    // Rimuove un dislike da una recensione
    public Review removeDislike(Long id) {
        return reviewRepository.findById(id)
            .map(review -> {
                review.setDislikesCount(review.getDislikesCount() - 1);
                return reviewRepository.save(review);
            }).orElseThrow(() -> new RuntimeException("Review not found"));
    }

    // Funzioni di utilità e funzioni per utente root
    // Cerca una recensione per id nella collection "reviews" del database
    // Provata: OK
    public Optional<Review> getReviewById(Long id){
        return reviewRepository.findById(id);
    }

    // Restituisce tutte le recensioni dalla collection "reviews" del database
    public ArrayList<Review> getAllReviews() {
        return (ArrayList<Review>) reviewRepository.findAll();
    }

    // Restituisce tutte le recensioni di un vino specifico di un'annata specifica dalla collection "reviews" del database
    public ArrayList<Review> getReviewsByVintage(Long wineId, int vintageYear) {
        return reviewRepository.findByWineId_IdAndWineId_Year(wineId, vintageYear);
    }

    // Restituisce tutte le recensioni di un vino specifico dalla collection "reviews" del database
    // Provata: OK
    public ArrayList<Review> getReviewsByWine(Long wineId) {
        return reviewRepository.findByWineId_Id(wineId);
    }

    // Restituisce tutte le recensioni di un utente specifico dalla collection "reviews" del database
    public ArrayList<Review> getReviewsByUser(String username) {
        return reviewRepository.findByUserId_Username(username);
    }

    // Restituisce tutte le recensioni di un utente specifico per un vino specifico dalla collection "reviews" del database
    public ArrayList<Review> getReviewsByUserAndWine(String username, Long wineId) {
        return reviewRepository.findByUserId_UsernameAndWineId_Id(username, wineId);
    }

    // Restituisce il numero di recensioni fatte per un determinato vino
    public Long getReviewsCountByWine(Long wineId) {
        return reviewRepository.countByWineId_Id(wineId);
    }

    // Restituisce il numero di recensioni di un determinato utente
    public Long getReviewsCountByUser(String username) {
        return reviewRepository.countByUserId_Username(username);
    }

    // Restituisce il numero di recensioni di una determinata annata di un vino
    public Long getReviewsCountByVintage(Long wineId, int vintageYear) {
        return reviewRepository.countByWineId_IdAndWineId_Year(wineId, vintageYear);
    }

    // Ordina e restituisce le recensioni ordinate sulla base del campo specificato, in ordine crescente o decrescente (terzo parametro)
    public ArrayList<Review> sortReviewsByField(ArrayList<Review> reviews, String field, boolean ascendingOrder) {
        reviews.sort((r1, r2) -> {
            switch (field) {
                case "rating":
                    return ascendingOrder ? Double.compare(r1.getRating(), r2.getRating()) : Double.compare(r2.getRating(), r1.getRating());
                case "createdAt":
                    return ascendingOrder ? r1.getCreatedAt().compareTo(r2.getCreatedAt()) : r2.getCreatedAt().compareTo(r1.getCreatedAt());
                case "likesCount":
                    return ascendingOrder ? Integer.compare(r1.getLikesCount(), r2.getLikesCount()) : Integer.compare(r2.getLikesCount(), r1.getLikesCount());
                case "dislikesCount":
                    return ascendingOrder ? Integer.compare(r1.getDislikesCount(), r2.getDislikesCount()) : Integer.compare(r2.getDislikesCount(), r1.getDislikesCount());
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
    public double getAverageRatingByWine(Long wineId, int year){
        ArrayList<Review> reviews = reviewRepository.findByWineId_IdAndWineId_Year(wineId, year);
        double sum = 0;
        for (Review review : reviews) {
            sum += review.getRating();
        }
        return sum / reviews.size();
    }

    // Restituisce le num recensioni più recenti
    public ArrayList<Review> getRecentReviews(int num) {
        ArrayList<Review> reviews = (ArrayList<Review>) reviewRepository.findAll();
        reviews.sort((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()));
        return new ArrayList<Review>(reviews.subList(0, num));
    }

    // Restituisce le num recensioni più recenti di un utente specifico
    public ArrayList<Review> getRecentReviewsByUser(String username, int num) {
        ArrayList<Review> reviews = reviewRepository.findByUserId_Username(username);
        reviews.sort((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()));
        return new ArrayList<Review>(reviews.subList(0, num));
    }

    // Restituisce le num recensioni più popolari (con più like) di un'annata specifica per un determinato vino
    public ArrayList<Review> getPopularReviewsByVintage(Long wineId, int vintageYear, int num) {
        ArrayList<Review> reviews = reviewRepository.findByWineId_IdAndWineId_Year(wineId, vintageYear);
        ArrayList<Review> popularReviews = sortReviewsByField(reviews, "likesCount", false);
        return new ArrayList<Review>(popularReviews.subList(0, num));
    }

    // Restituisce le num recensioni più recenti di un'annata specifica per un determinato vino
    public ArrayList<Review> getRecentReviewsByVintage(Long wineId, int vintageYear, int num) {
        ArrayList<Review> reviews = reviewRepository.findByWineId_IdAndWineId_Year(wineId, vintageYear);
        ArrayList<Review> recentReviews = sortReviewsByField(reviews, "createdAt", false);
        return new ArrayList<Review>(recentReviews.subList(0, num));
    }

    // Restituisce le recensioni di un vino specifico in un range di rating specifico
    public ArrayList<Review> getReviewsByWineAndRatingRange(Long wineId, double minRating, double maxRating) {
        return reviewRepository.findByWineId_IdAndRatingBetween(wineId, minRating, maxRating);
    }

    // Restituisce le recensioni di un'annata specifica per un determinato vino in un range di rating specifico
    public ArrayList<Review> getReviewsByVintageAndRatingRange(Long wineId, int year, double minRating, double maxRating) {
        return reviewRepository.findByWineId_IdAndWineId_YearAndRatingBetween(wineId, year, minRating, maxRating);
    }

    // Cancella tutte le recensioni
    public void deleteAllReviews() {
        reviewRepository.deleteAll();
    }

    // Cancella una recensione specifica
    public void deleteReviewById(Long id) {
        reviewRepository.deleteById(id);
    }

    // Cancella tutte le recensioni di un vino specifico
    public void deleteReviewsByWine(Long wineId) {
        reviewRepository.deleteByWineId_Id(wineId);
    }

    // Cancella tutte le recensioni di un utente specifico
    public void deleteReviewsByUser(String username) {
        reviewRepository.deleteByUserId_Username(username);
    }

    // Cancella tutte le recensioni di un'annata specifica di un vino specifico
    public void deleteReviewsByVintage(Long wineId, int vintageYear) {
        reviewRepository.deleteByWineId_IdAndWineId_Year(wineId, vintageYear);
    }

}
