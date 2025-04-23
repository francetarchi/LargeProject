package com.wineadvisor.wineadvisor.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.wineadvisor.wineadvisor.model.Winery;

@Repository
public interface WineryRepository extends MongoRepository<Winery, Long> {
    Optional<Winery> findByLogin_Username(String username);
}
