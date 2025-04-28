package com.wineadvisor.wineadvisor.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.wineadvisor.wineadvisor.model.wines.fields.Country;

import java.util.*;

@Repository
public interface CountryRepository extends MongoRepository<Country, Long> {

    public Optional<Country> findByName(String country);
    
}
