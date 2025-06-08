package com.wineadvisor.wineadvisor.repository;

import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.wineadvisor.wineadvisor.model.countries.Country;


@Repository
public interface CountryRepository extends MongoRepository<Country, ObjectId> {

    Optional<Country> findByName(String country);
    
}
