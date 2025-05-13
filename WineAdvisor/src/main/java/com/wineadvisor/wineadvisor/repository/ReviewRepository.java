package com.wineadvisor.wineadvisor.repository;

import java.util.ArrayList;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.wineadvisor.wineadvisor.model.reviews.Review;


@Repository
public interface ReviewRepository extends MongoRepository<Review, Long> {

    Page<Review> findByWineId_IdAndWineId_Year(Pageable pageable, Long wineId, Integer year);
    ArrayList<Review> findByWineId_IdAndWineId_Year(Long wineId, Integer year);
    
    Page<Review> findByWineId_Id(Pageable pageable, Long wineId);
    ArrayList<Review> findByWineId_Id(Long wineId);

    Page<Review> findByUserId_Username(Pageable pageable, String username);
    ArrayList<Review> findByUserId_Username(String username);

    Page<Review> findByUserId_UsernameAndWineId_Id(Pageable pageable, String username, Long wineId);

    Long countByWineId_Id(Long wineId);

    Long countByUserId_Username(String username);

    Long countByWineId_IdAndWineId_Year(Long wineId, Integer year);

    Page<Review> findByWineId_IdAndRatingBetween(Pageable pageable, Long wineId, Double minRating, Double maxRating);

    Page<Review> findByWineId_IdAndWineId_YearAndRatingBetween(Pageable pageable, Long wineId, Integer year, Double minRating, Double maxRating);

    Optional<Review> findByIdAndUserId_Username(Long id, String username);
    
    Optional<Review> findByUserId_UsernameAndWineId_IdAndWineId_Year(String username, Long wineId, Integer year);   

    void deleteAllByWineId_Id(Long wineId);
    
    void deleteAllByUserId_Username(String username);

    void deleteAllByWineId_IdAndWineId_Year(Long wineId, Integer year);
}
