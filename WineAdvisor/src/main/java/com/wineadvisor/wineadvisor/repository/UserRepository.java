package com.wineadvisor.wineadvisor.repository;

import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.wineadvisor.wineadvisor.model.users.User;

import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.repository.MongoRepository;

@Repository
public interface UserRepository extends MongoRepository<User, ObjectId> {
    Optional<User> findByLogin_Username(String username);
    Optional<User> findByEmail(String email);

    Page<User> findByName_FirstAndName_Last(String firstName, String lastName, Pageable pageable);
    Page<User> findByName_First(String firstName, Pageable pageable);
    Page<User> findByName_Last(String lastName, Pageable pageable);
    
    Optional<User> findByReviews_ReviewId(Long id);
    Optional<User> findByReviews_WineId_Id(Long wineId);
}
