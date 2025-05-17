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
    // metodo che restituisce una lista di recensioni relative ad un'annata di un vino specifica
    Page<Review> findByWineId_IdAndWineId_Year(Pageable pageable, Long wineId, Integer year);
    ArrayList<Review> findByWineId_IdAndWineId_Year(Long wineId, Integer year);
    
    // metodo che restituisce una lista di recensioni relative ad un vino
    Page<Review> findByWineId_Id(Pageable pageable, Long wineId);
    ArrayList<Review> findByWineId_Id(Long wineId);

    // metodo che restituisce una lista di recensioni relative ad un utente
    Page<Review> findByUserId_Username(Pageable pageable, String username);
    ArrayList<Review> findByUserId_Username(String username);

    // metodo che restituisce una lista di recensioni relative ad un utente e ad un vino
    Page<Review> findByUserId_UsernameAndWineId_Id(Pageable pageable, String username, Long wineId);

    // metodo che restituisce il conto delle recensioni relative ad un vino
    Long countByWineId_Id(Long wineId);

    // metodo che restituisce il conto delle recensioni relative ad un utente
    Long countByUserId_Username(String username);

    // metodo che restituisce il conto delle recensioni relative ad un'annata di un vino
    Long countByWineId_IdAndWineId_Year(Long wineId, Integer year);

    // metodo che restituisce le recensioni di un vino sotto un certo rating e sopra un certo rating
    Page<Review> findByWineId_IdAndRatingBetween(Pageable pageable, Long wineId, Double minRating, Double maxRating);

    // metodo che restituisce le recensioni di un'annata specifica di un determinato vino sotto un certo rating e sopra un certo rating
    Page<Review> findByWineId_IdAndWineId_YearAndRatingBetween(Pageable pageable, Long wineId, Integer year, Double minRating, Double maxRating);

    // metodo che restituisce una recensione per id e nome utente
    Optional<Review> findByIdAndUserId_Username(Long id, String username);
    
    // metodo che restituisce una recensione per nome utente e vintage
    Optional<Review> findByUserId_UsernameAndWineId_IdAndWineId_Year(String username, Long wineId, Integer year);   
    Optional<Review> findByUserId_UsernameAndWineId_NameAndWineId_Year(String username, String wineName, Integer year);

    // metodo che elimina tutte le recensioni con un determinato wine id
    void deleteAllByWineId_Id(Long wineId);
    
    // metodo che elimina tutte le recensioni di un utente
    void deleteAllByUserId_Username(String username);

    // metodo che elimina tutte le recensioni di un'annata specifica di un determinato vino
    void deleteAllByWineId_IdAndWineId_Year(Long wineId, Integer year);
}
