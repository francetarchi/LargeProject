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

    Optional<Wine> findByIdAndVintages_Year(Long id, Integer year);

    Optional<Wine> findByVintages_Reviews_ReviewId(Long id);

    Page<Wine> findByVintages_PriceBetween(Pageable pageable, Double min_price, Double max_price);

    Page<Wine> findByWinery_Username(Pageable pageable, String username);

    Page<Wine> findByRegion_Name(Pageable pageable, String region);
    ArrayList<Wine> findByRegion_Name(String region);

    Page<Wine> findByStatistics_RatingsAverageGreaterThanEqual(Pageable pageable, Double minRating);

    Page<Wine> findByNameContainingIgnoreCase(Pageable pageable, String keyword);

    Page<Wine> findByStyle_Grapes_Name(Pageable pageable, String grapeName);

    Page<Wine> findByRegion_Country_Name(Pageable pageable, String country);
    ArrayList<Wine> findByRegion_Country_Name(String country);

    Page<Wine> findByType(Pageable pageable, String type);
    
    Optional<Wine> findByVintages(Long id, Integer year);

    ArrayList<Wine> findByWinery_Username(String username);

    Optional<Wine> findByIdAndWinery_Username(Long wineId, String username);

    Page<Wine> findByNameContainingIgnoreCaseAndWinery_UsernameContainingIgnoreCaseAndRegion_NameContainingIgnoreCaseAndRegion_Country_NameContainingIgnoreCaseAndTypeContainingIgnoreCaseAndStyle_Grapes_NameContainingIgnoreCaseAndStatistics_RatingsAverageGreaterThanEqualAndVintages_PriceBetween(Pageable pageable, String name, String winery, String region, String country, String type, String grape, Double minAverageRating, Double min, Double max);

    ArrayList<Wine> findByStyle_Name(String style_name);

}
