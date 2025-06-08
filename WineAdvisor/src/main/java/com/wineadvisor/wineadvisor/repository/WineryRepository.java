package com.wineadvisor.wineadvisor.repository;

import java.util.ArrayList;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.wineadvisor.wineadvisor.model.wineries.Winery;


@Repository
public interface WineryRepository extends MongoRepository<Winery, ObjectId> {
    Optional<Winery> findByLogin_Username(String username);
    Optional<Winery> findByEmail(String email);
    
    Page<Winery> findByNameContainingIgnoreCase(String name, PageRequest of);

    ArrayList<Winery> findByRegion(String region);
    
    ArrayList<Winery> findByCountry(String name);
    
    boolean existsByLogin_Username(String username);
}
