package com.wineadvisor.wineadvisor.repository;

import java.util.ArrayList;
import java.util.Optional;

import org.bson.types.ObjectId;

import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.wineadvisor.wineadvisor.model.User;

@Repository
public interface UserRepository extends MongoRepository<User, ObjectId> {
    Optional<User> findByLogin_Username(String username);
    Optional<User> findByEmail(String email);

    ArrayList<User> findByName_First(String name);
    ArrayList<User> findByName_Last(String last_name);
    Optional<User> findByReviews_Id(Long id);
    Optional<User> findByReviews_WineId_Id(Long wineId);
}
