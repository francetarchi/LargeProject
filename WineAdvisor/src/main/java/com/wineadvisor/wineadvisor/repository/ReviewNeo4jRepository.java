package com.wineadvisor.wineadvisor.repository;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.OffsetDateTime;

import org.springframework.stereotype.Repository;
import org.springframework.data.neo4j.core.Neo4jClient;

import com.wineadvisor.wineadvisor.model.reviews.Review;

import lombok.RequiredArgsConstructor;


@Repository
@RequiredArgsConstructor
public class ReviewNeo4jRepository {
    private final Neo4jClient neo4jClient;


    public void createReviewForUser(String username, String firstName, String lastName, Long reviewId, String text, Double rating, Long wineId, String wineName, Integer wineYear, String wineImage, String thumbnail, Instant createdAtInstant) {
        OffsetDateTime createdAt = createdAtInstant.atOffset(ZoneOffset.UTC);
        
        // Recupero tutte le review esistenti dell'utente ordinate per ID crescente
        List<Map<String, Object>> existingReviews = new ArrayList<>(
            neo4jClient.query("""
                MATCH (u:User {username: $username})-[:WROTE]->(r:Review)
                RETURN id(r) AS id
                ORDER BY id(r) ASC
            """)
            .bind(username).to("username")
            .fetch()
            .all()
        );

        // Se ci sono già 3 o più recensioni, elimino la più vecchia
        if (existingReviews.size() >= 3) {
            Long idToDelete = (Long) existingReviews.get(0).get("id");
            neo4jClient.query("""
                MATCH (r:Review)
                WHERE id(r) = $id
                DETACH DELETE r
            """)
            .bind(idToDelete).to("id")
            .run();
        }

        // Creo la nuova recensione (se il nodo dell'utente non è già presente viene creato)
        Map<String, Object> params = new HashMap<>();
        params.put("username", username);
        params.put("firstName", firstName);
        params.put("lastName", lastName);
        params.put("reviewId", reviewId);
        params.put("text", text);
        params.put("rating", rating);
        params.put("wineId", wineId);
        params.put("wineName", wineName);
        params.put("wineYear", wineYear);
        params.put("wineImage", wineImage);
        params.put("thumbnail", thumbnail);
        params.put("createdAt", createdAt);
        

        neo4jClient.query("""
            MERGE (u:User {username: $username})
            ON CREATE SET 
                u.firstName = $firstName,
                u.lastName = $lastName,
                u.thumbnail = $thumbnail
            CREATE (r:Review {
                text: $text,
                rating: $rating,
                reviewId: $reviewId,
                wineId: $wineId,
                wineName: $wineName,
                wineYear: $wineYear,
                wineImage: $wineImage,
                createdAt: $createdAt
            })
            CREATE (u)-[:WROTE]->(r)
        """)
        .bindAll(params)
        .run();
    }

    public void updateReview(Long reviewId, String newText, Double newRating) {
        Map<String, Object> params = new HashMap<>();
        params.put("reviewId", reviewId);
        params.put("newText", newText);
        params.put("newRating", newRating);

        neo4jClient.query("""
            MATCH (r:Review)
            WHERE r.reviewId = $reviewId
            SET r.text = $newText,
                r.rating = $newRating
        """)
        .bindAll(params)
        .run();
    }

    public void updateRecentReviewsForUser(String username, String firstName, String lastName, String thumbnail, ArrayList<Review> recentReviews) {

        // Creo o aggiorno l'utente
        neo4jClient.query("""
            MERGE (u:User {username: $username})
            ON CREATE SET 
                u.firstName = $firstName,
                u.lastName = $lastName,
                u.thumbnail = $thumbnail
            ON MATCH SET 
                u.firstName = $firstName,
                u.lastName = $lastName,
                u.thumbnail = $thumbnail
        """)
        .bind(username).to("username")
        .bind(firstName).to("firstName")
        .bind(lastName).to("lastName")
        .bind(thumbnail).to("thumbnail")
        .run();

        // Elimino le vecchie recensioni dell'utente
        neo4jClient.query("""
            MATCH (u:User {username: $username})-[:WROTE]->(r:Review)
            DETACH DELETE r
        """)
        .bind(username).to("username")
        .run();

        // Creo le nuove recensioni collegate all'utente
        for (Review r : recentReviews) {
            Map<String, Object> params = new HashMap<>();
            params.put("username", username);
            params.put("reviewId", r.getId());
            params.put("text", r.getText());
            params.put("rating", r.getRating());
            params.put("wineId", r.getWineId().getId());
            params.put("wineName", r.getWineId().getName());
            params.put("wineYear", r.getWineId().getYear());
            params.put("wineImage", r.getWineId().getImage());

            Instant createdAtInstant = r.getCreatedAt();

            OffsetDateTime createdAt = createdAtInstant.atOffset(ZoneOffset.UTC);

            params.put("createdAt", createdAt);

            neo4jClient.query("""
                MATCH (u:User {username: $username})
                CREATE (r:Review {
                    text: $text,
                    rating: $rating,
                    reviewId: $reviewId,
                    wineId: $wineId,
                    wineName: $wineName,
                    wineYear: $wineYear,
                    wineImage: $wineImage,
                    createdAt: $createdAt
                })
                CREATE (u)-[:WROTE]->(r)
            """)
            .bindAll(params)
            .run();
        }
    }

    public void deleteAllReviewsByUser(String username) {
        neo4jClient.query("""
            MATCH (u:User {username: $username})-[:WROTE]->(r:Review)
            DETACH DELETE r
        """)
        .bind(username).to("username")
        .run();
    }

    public void deleteReviewById(Long reviewId) {
        neo4jClient.query("""
            MATCH (r:Review)
            WHERE r.reviewId = $reviewId
            DETACH DELETE r
        """)
        .bind(reviewId).to("reviewId")
        .run();
    }

    public void deleteAllReviewsByWine(Long wineId) {
        neo4jClient.query("""
            MATCH (r:Review {wineId: $wineId})
            DETACH DELETE r
        """)
        .bind(wineId).to("wineId")
        .run();
    }

    public void deleteAllReviewsByVintage(Long wineId, Integer vintage) {
        Map<String, Object> params = new HashMap<>();
        params.put("wineId", wineId);
        params.put("vintage", vintage);

        if(vintage != null) {
            neo4jClient.query("""
                MATCH (r:Review {wineId: $wineId})
                WHERE r.wineYear = $vintage
                DETACH DELETE r
            """)
            .bindAll(params)
            .run();
        } else {
            neo4jClient.query("""
                MATCH (r:Review {wineId: $wineId})
                WHERE r.wineYear IS NULL
                DETACH DELETE r
            """)
            .bind(wineId).to("wineId")
            .run();
        }        
    }

    public void deleteAllReviews() {
        neo4jClient.query("""
            MATCH (r:Review)
            DETACH DELETE r
        """)
        .run();
    }

    public List<Map<String, Object>> getPaginatedFeed(String username, Integer skip, Integer limit) {
        String query = """
            MATCH (me:User {username: $username})-[:FOLLOWS]->(friend:User)
            MATCH (friend)-[:WROTE]->(review:Review)
            WITH friend, review
            ORDER BY review.created_at DESC
            WITH friend, collect(review)[0..3] AS recentReviews
            UNWIND recentReviews AS review
            RETURN {
                authorUsername: friend.username,
                authorThumbnail: friend.thumbnail,
                wineName: review.wineName,
                wineYear: review.wineYear,
                wineId: review.wineId,
                reviewText: review.text,
                reviewId: review.reviewId,
                rating: review.rating,
                wineImage: review.image,
                date: review.created_at
            } AS result
            ORDER BY result.date DESC
            SKIP $skip
            LIMIT $limit
        """;

        return new ArrayList<>(
            neo4jClient.query(query)
                .bindAll(
                    Map.of(
                        "username", username,
                        "skip", skip,
                        "limit", limit
                    )
                )
                .fetch()
                .all()
        );
    }
}
