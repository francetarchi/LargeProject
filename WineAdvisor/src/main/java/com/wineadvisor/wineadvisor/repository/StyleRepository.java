package com.wineadvisor.wineadvisor.repository;

import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.wineadvisor.wineadvisor.model.styles.Style;


@Repository
public interface StyleRepository extends MongoRepository<Style, ObjectId> {

    Optional<Style> findByName(String style);

    void deleteByName(String style_name);
    
}
