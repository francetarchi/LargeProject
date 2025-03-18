package com.wineadvisor.wineadvisor.repository;

import com.wineadvisor.wineadvisor.model.Review;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public interface ReviewRepository extends MongoRepository<Review, Long> { 
    
    // metodo findByWineId_IdAndWineId_Year: restituisce una lista di recensioni relative ad un'annata di un vino specifica
    ArrayList<Review> findByWineId_IdAndWineId_Year(Long wineId, int year);
    
    // metodo findByWineId_Id: restituisce una lista di recensioni relative ad un vino
    ArrayList<Review> findByWineId_Id(Long wineId);

    // metodo findByUserId_Username: restituisce una lista di recensioni relative ad un utente
    ArrayList<Review> findByUserId_Username(String username);

    // metodo findByUserId_UsernameAndWineId_Id: restituisce una lista di recensioni relative ad un utente e ad un vino
    ArrayList<Review> findByUserId_UsernameAndWineId_Id(String username, Long wineId);
}
