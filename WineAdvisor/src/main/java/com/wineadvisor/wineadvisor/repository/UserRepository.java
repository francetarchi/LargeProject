package com.wineadvisor.wineadvisor.repository;

import java.util.ArrayList;
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
    Optional<User> findByReviews_ReviewId(Long id);

    Page<User> findByName_FirstContainingIgnoreCaseAndName_LastContainingIgnoreCase(String firstName, String lastName, Pageable pageable);
    Page<User> findByName_FirstContainingIgnoreCase(String name, Pageable pageable);
    Page<User> findByName_LastContainingIgnoreCase(String last_name, Pageable pageable);

    ArrayList<User> findByReviews_WineId_Id(Long wineId);
    ArrayList<User> findByReviews_WineId_IdAndReviews_WineId_Year(Long wineId, Integer vintageYear);
    ArrayList<User> findByLikesOrDislikes(Long id);
    ArrayList<User> findByAddress_Region(String region);
}
