package com.wineadvisor.wineadvisor.repository;

import java.util.ArrayList;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.wineadvisor.wineadvisor.model.User;

@Repository
public interface UserRepository extends MongoRepository<User, ObjectId> {
    Optional<User> findByLogin_Username(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByReviews_ReviewId(Long id);

    Page<User> findByName_FirstAndName_Last(String firstName, String lastName, Pageable pageable);
    Page<User> findByName_First(String name, Pageable pageable);
    Page<User> findByName_Last(String last_name, Pageable pageable);

    ArrayList<User> findByReviews_WineId_Id(Long wineId);
    ArrayList<User> findByReviews_WineId_IdAndReviews_WineId_Year(Long wineId, Integer vintageYear);
}
