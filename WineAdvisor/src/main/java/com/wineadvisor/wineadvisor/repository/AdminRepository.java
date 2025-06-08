package com.wineadvisor.wineadvisor.repository;

import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.wineadvisor.wineadvisor.model.admin.Admin;


@Repository
public interface AdminRepository extends MongoRepository<Admin, ObjectId> {
    Optional<Admin> findByLogin_Username(String username);
    Optional<Admin> findByEmail(String email);
}
