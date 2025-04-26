package com.wineadvisor.wineadvisor.repository;

import java.util.ArrayList;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.wineadvisor.wineadvisor.model.wines.Wine;

@Repository
public interface WineRepository extends MongoRepository<Wine, Long> {

    Optional<Wine> findByVintages_Reviews_ReviewId(Long id);

    Optional<Wine> findByIdAndVintages_Year(Long id, Integer year);

    Optional<Wine> findByVintages(Long id, Integer year);

    ArrayList<Wine> findByWinery_Username(String username);
}
