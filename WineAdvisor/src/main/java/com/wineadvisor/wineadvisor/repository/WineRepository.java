package com.wineadvisor.wineadvisor.repository;

import com.wineadvisor.wineadvisor.model.Wine;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WineRepository extends MongoRepository<Wine, Long> {

    Optional<Wine> findByVintages_Reviews_Review_id(Long id);

    Optional<Wine> findByIdAndVintages_Year(Long id, Integer year);

    Optional<Wine> findByVintages(Long id, Integer year);

    Optional<Wine> findbyVintages_Reviews_Id(Long id);

}
