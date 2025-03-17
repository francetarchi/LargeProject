package com.wineadvisor.wineadvisor.repository;

import com.wineadvisor.wineadvisor.model.Wine;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WineRepository extends MongoRepository<Wine, Long> {
}
