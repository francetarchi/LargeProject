package com.wineadvisor.wineadvisor.repository;

import java.util.ArrayList;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.wineadvisor.wineadvisor.model.User;

@Repository
public interface UserRepository extends MongoRepository<User, ObjectId> {
    Optional<User> findByLogin_Username(String username);

    ArrayList<User> findByName_First(String name);
    ArrayList<User> findByName_Last(String last_name);
}
