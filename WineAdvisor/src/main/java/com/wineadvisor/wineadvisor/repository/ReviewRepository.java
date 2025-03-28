package com.wineadvisor.wineadvisor.repository;

import com.wineadvisor.wineadvisor.model.Review;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public interface ReviewRepository extends MongoRepository<Review, Long> { 

    // metodo findById: restituisce una recensione dato l'id
    Optional<Review> findById(Long id);

    // metodo deleteById: elimina una recensione dato l'id
    void deleteById(Long id);
    
    // metodo findByWineId_IdAndWineId_Year: restituisce una lista di recensioni relative ad un'annata di un vino specifica
    ArrayList<Review> findByWineId_IdAndWineId_Year(Long wineId, Integer year);
    
    // metodo findByWineId_Id: restituisce una lista di recensioni relative ad un vino
    ArrayList<Review> findByWineId_Id(Long wineId);

    // metodo findByUserId_Username: restituisce una lista di recensioni relative ad un utente
    ArrayList<Review> findByUserId_Username(String username);

    // metodo findByUserId_UsernameAndWineId_Id: restituisce una lista di recensioni relative ad un utente e ad un vino
    ArrayList<Review> findByUserId_UsernameAndWineId_Id(String username, Long wineId);

    // metodo che restituisce il conto delle recensioni relative ad un vino
    Long countByWineId_Id(Long wineId);

    // metodo che restituisce il conto delle recensioni relative ad un utente
    Long countByUserId_Username(String username);

    // metodo che restituisce il conto delle recensioni relative ad un'annata di un vino
    Long countByWineId_IdAndWineId_Year(Long wineId, Integer year);

    // metodo che restituisce le recensioni di un vino sotto un certo rating e sopra un certo rating
    ArrayList<Review> findByWineId_IdAndRatingBetween(Long wineId, Double minRating, Double maxRating);

    // metodo che restituisce le recensioni di un'annata specifica di un determinato vino sotto un certo rating e sopra un certo rating
    ArrayList<Review> findByWineId_IdAndWineId_YearAndRatingBetween(Long wineId, Integer year, Double minRating, Double maxRating);

    // metodo che elimina tutte le recensioni di un vino
    void deleteByWineId_Id(Long wineId);

    // metodo che elimina tutte le recensioni di un utente
    void deleteByUserId_Username(String username);

    // metodo che elimina tutte le recensioni di un'annata specifica di un determinato vino
    void deleteByWineId_IdAndWineId_Year(Long wineId, Integer year);

    Optional<Review> findByIdAndUserId_Username(Long id, String username);
}