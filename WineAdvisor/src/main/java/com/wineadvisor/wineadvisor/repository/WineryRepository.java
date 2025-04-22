package com.wineadvisor.wineadvisor.repository;

import com.wineadvisor.wineadvisor.model.Winery;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WineryRepository extends MongoRepository<Winery, Long> {    
    
}
