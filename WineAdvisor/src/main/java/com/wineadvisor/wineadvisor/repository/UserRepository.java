package com.wineadvisor.wineadvisor.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.wineadvisor.wineadvisor.model.User;

public interface UserRepository extends MongoRepository<User, Long> {
    
}
