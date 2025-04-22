package com.wineadvisor.wineadvisor.repository;

import com.wineadvisor.wineadvisor.model.fields.wines.Country;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public interface CountryRepository extends MongoRepository<Country, Long> {

    public Optional<Country> findByName(String country);
    
}
