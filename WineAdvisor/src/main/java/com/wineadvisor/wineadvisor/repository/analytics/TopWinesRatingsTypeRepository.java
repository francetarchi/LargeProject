package com.wineadvisor.wineadvisor.repository.analytics;

import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.wineadvisor.wineadvisor.model.analytics.TopWinesRatingsType;

@Repository
public interface TopWinesRatingsTypeRepository extends MongoRepository<TopWinesRatingsType, ObjectId> {
    Optional<TopWinesRatingsType> findByType(String type);

    @Query(value = "{ 'type' : ?0 }", fields = "{ 'type': 1, 'wines': { $slice: [ { $filter: { input: '$wines', as: 'wine', cond: { $lte: ['$$wine.prices_average', ?2] } } }, ?1 ] } }")
    Optional<TopWinesRatingsType> findByTypeLimitN(String type, final Integer N, Double maxPrice);
}
