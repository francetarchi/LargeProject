package com.wineadvisor.wineadvisor.repository_neo4j;

import lombok.RequiredArgsConstructor;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;


@Repository
@RequiredArgsConstructor
public class ReviewNeo4jRepository {

    private final Neo4jClient neo4jClient;

    public void createReviewForUser(String username, String text, double rating, String wineName, int wineYear, String wineImage, String userThumbnail) {
        // 1. Recupera tutte le review esistenti dell'utente ordinate per ID crescente
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

        // 2. Se ci sono già 3 o più recensioni, elimina la più vecchia
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

        // 3. Crea la nuova recensione
        neo4jClient.query("""
            MATCH (u:User {username: $username})
            CREATE (r:Review {
                text: $text,
                rating: $rating,
                wineName: $wineName,
                wineYear: $wineYear,
                wineImage: $wineImage,
                userThumbnail: $userThumbnail
            })
            CREATE (u)-[:WROTE]->(r)
        """)
        .bindAll(Map.of(
            "username", username,
            "text", text,
            "rating", rating,
            "wineName", wineName,
            "wineYear", wineYear,
            "wineImage", wineImage,
            "userThumbnail", userThumbnail
        ))
        .run();
    }

    public List<Map<String, Object>> getReviewsByUser(String username) {
        return new ArrayList<>(neo4jClient.query("""
            MATCH (u:User {username: $username})-[:WROTE]->(r:Review)
            RETURN r.text AS text, r.rating AS rating, r.wineName AS wineName, r.wineYear AS wineYear,
                   r.wineImage AS wineImage, r.userThumbnail AS userThumbnail
            ORDER BY id(r) DESC
            LIMIT 3
        """)
        .bind(username).to("username")
        .fetch()
        .all());
    }


    public void updateReview(String username, String wineName, int wineYear, String newText, double newRating) {
        neo4jClient.query("""
            MATCH (u:User {username: $username})-[:WROTE]->(r:Review {wineName: $wineName, wineYear: $wineYear})
            SET r.text = $newText,
                r.rating = $newRating
        """)
        .bindAll(Map.of(
            "username", username,
            "wineName", wineName,
            "wineYear", wineYear,
            "newText", newText,
            "newRating", newRating
        ))
        .run();
    }



    public void deleteAllReviewsByUser(String username) {
        neo4jClient.query("""
            MATCH (u:User {username: $username})-[:WROTE]->(r:Review)
            DETACH DELETE r
        """)
        .bind(username).to("username")
        .run();
    }
}
