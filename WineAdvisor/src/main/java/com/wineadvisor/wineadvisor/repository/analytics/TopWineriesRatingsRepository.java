package com.wineadvisor.wineadvisor.repository.analytics;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.wineadvisor.wineadvisor.model.analytics.TopWineriesRatings;

@Repository
public interface TopWineriesRatingsRepository extends MongoRepository<TopWineriesRatings, ObjectId> {
    Optional<TopWineriesRatings> findByWineryUsername(String wineryUsername);

    List<TopWineriesRatings> findFirst10By();
    List<TopWineriesRatings> findFirst20By();
    List<TopWineriesRatings> findFirst50By();
    List<TopWineriesRatings> findFirst100By();
    List<TopWineriesRatings> findFirst1000By();
}
