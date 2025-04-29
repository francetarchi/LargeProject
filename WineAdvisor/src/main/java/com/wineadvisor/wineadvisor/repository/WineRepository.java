package com.wineadvisor.wineadvisor.repository;

import java.util.ArrayList;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.wineadvisor.wineadvisor.model.wines.Wine;

@Repository
public interface WineRepository extends MongoRepository<Wine, Long> {
    // Metodo per trovare un vino in base al suo id e all'annata
    Optional<Wine> findByIdAndVintages_Year(Long id, Integer year);

    // Metodo per trovare un vino in base all'id di una recensione
    Optional<Wine> findByVintages_Reviews_ReviewId(Long id);

    // Metodo per trovare un vino in base ad un range di prezzi
    Page<Wine> findByVintages_PriceBetween(Pageable pageable, Double min_price, Double max_price);

    // Metodo per trovare un vino in base allo username della cantina
    Page<Wine> findByWinery_Username(Pageable pageable, String username);

    // Metodo per trovare un vino in base al nome della region
    Page<Wine> findByRegion_Name(Pageable pageable, String region);
    ArrayList<Wine> findByRegion_Name(String region);

    // Metodo per trovare un vino in base al rating medio delle recensioni
    Page<Wine> findByStatistics_RatingsAverageGreaterThanEqual(Pageable pageable, Double minRating);

    // Metodo per trovare un vino in base ad una keyword nel nome (case insensitive)
    Page<Wine> findByNameContainingIgnoreCase(Pageable pageable, String keyword);

    // Metodo per trovare un vino in base al nome del vitigno
    Page<Wine> findByStyle_Grapes_Name(Pageable pageable, String grapeName);

    // Metodo per trovare un vino in base al nome del country
    Page<Wine> findByRegion_Country_Name(Pageable pageable, String country);

    // Metodo per trovare un vino in base al tipo
    Page<Wine> findByType(Pageable pageable, String type);
    

    Optional<Wine> findByVintages(Long id, Integer year);

    ArrayList<Wine> findByWinery_Username(String username);
}
