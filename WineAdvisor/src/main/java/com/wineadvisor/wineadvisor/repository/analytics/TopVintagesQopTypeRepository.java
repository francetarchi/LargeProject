package com.wineadvisor.wineadvisor.repository.analytics;

import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.wineadvisor.wineadvisor.model.analytics.TopVintagesQopType;

@Repository
public interface TopVintagesQopTypeRepository extends MongoRepository<TopVintagesQopType, ObjectId> {
    Optional<TopVintagesQopType> findByType(String type);

    @Query(value = "{ 'type' : ?0 }", fields = "{ 'type': 1, 'vintages': { $slice: ?1 } }")
    Optional<TopVintagesQopType> findByTypeLimitN(String type, final Integer N);
}
