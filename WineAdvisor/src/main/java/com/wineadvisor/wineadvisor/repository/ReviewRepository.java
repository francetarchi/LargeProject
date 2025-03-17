package com.wineadvisor.wineadvisor.repository;

import com.wineadvisor.wineadvisor.model.Review;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends MongoRepository<Review, Long> {    
}
