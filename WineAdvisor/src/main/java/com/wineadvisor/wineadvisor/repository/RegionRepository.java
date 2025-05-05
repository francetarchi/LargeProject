package com.wineadvisor.wineadvisor.repository;

import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.wineadvisor.wineadvisor.model.regions.Region;

@Repository
public interface RegionRepository extends MongoRepository<Region, ObjectId> {
    Optional<Region> findByName(String name);
    Optional<Region> findByNameAndCountry(String name, String country);
    Page<Region> findByCountry(Pageable pageable, String country);
    void deleteByName(String name);
    void deleteAllByCountry(String country);
}
