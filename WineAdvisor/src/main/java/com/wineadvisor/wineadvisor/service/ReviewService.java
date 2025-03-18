package com.wineadvisor.wineadvisor.service;

import com.wineadvisor.wineadvisor.repository.ReviewRepository;
import com.mongodb.client.MongoDatabase;
import com.wineadvisor.wineadvisor.model.Review;
import com.wineadvisor.wineadvisor.model.WineId;

import lombok.RequiredArgsConstructor;

import java.util.*;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final MongoDatabase database;

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

    // Funzioni di utilità e per utente root
    // Cerca una recensione per id nella collection "reviews" del database
    public Optional<Review> getReviewById(Long id){
        return reviewRepository.findById(id);
    }

    // Ottiene tutte le recensioni dalla collection "reviews" del database
    public ArrayList<Review> getAllReviews() {
        return (ArrayList<Review>) reviewRepository.findAll();
    }

    // Ottiene tutte le recensioni di un vino specifico di un'annata specifica dalla collection "reviews" del database
    public ArrayList<Review> getReviewsByWineAndYear(Long wineId, int vintageYear) {
        return reviewRepository.findByWineId_IdAndWineId_Year(wineId, vintageYear);
    }

    // Ottiene tutte le recensioni di un vino specifico dalla collection "reviews" del database
    public ArrayList<Review> getReviewsByWine(Long wineId) {
        return reviewRepository.findByWineId_Id(wineId);
    }

    // Ottiene tutte le recensioni di un utente specifico dalla collection "reviews" del database
    public ArrayList<Review> getReviewsByUser(String username) {
        return reviewRepository.findByUserId_Username(username);
    }

    // Ottiene tutte le recensioni di un utente specifico per un vino specifico dalla collection "reviews" del database
    public ArrayList<Review> getReviewsByUserAndWine(String username, Long wineId) {
        return reviewRepository.findByUserId_UsernameAndWineId_Id(username, wineId);
    }

    // Ottiene il numero totale di recensioni fatte per un determinato vino
    public ArrayList<Review> getReviewsCountByWine(Long wineId) {
        
    }

    // Ottiene il numero totale di recensioni di un determinato utente
    public ArrayList<Review> getReviewsCountByUser(String username) {
        
    }

    // Calcola e restituisce la media dei rating di un vino
    public double getAverageRatingByWine(Long wineId) {
        
    }

    // Restituisce le num recensioni più recenti
    public ArrayList<Review> getRecentReviews(int num) {
        
    }

    // Restituisce le num recensioni più recenti di un utente specifico
    public ArrayList<Review> getRecentReviewsByUser(String username, int num) {
        
    }

    // Restituisce le num recensioni più popolari (con più like) di un vino specifico di un'annata specifica
    public ArrayList<Review> getPopularReviewsByWineAndYear(int wineId, int vintageYear, int num) {
        
    }

    // Restituisce le 3 recensioni più recenti di un'annata specifica di un vino specifico
    public ArrayList<Review> getRecentReviewsByWineAndYear(int wineId, int vintageYear) {
        
    }

    // Restituisce le recensioni di un vino specifico in un range di rating specifico
    public ArrayList<Review> getReviewsByWineAndRatingRange(int wineId, double minRating, double maxRating) {
        
    }

    // Cancella tutte le recensioni
    public void deleteAllReviews() {
        reviewRepository.deleteAll();
    }

    // Cancella tutte le recensioni di un vino specifico
    public void deleteReviewsByWine(Long wineId) {
        
    }

    // Cancella tutte le recensioni di un utente specifico
    public void deleteReviewsByUser(String username) {
        
    }

    // Cancella tutte le recensioni di un'annata specifica di un vino specifico
    public void deleteReviewsByWineAndYear(Long wineId, int vintageYear) {
        
    }

}
