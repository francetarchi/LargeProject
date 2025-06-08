package com.wineadvisor.wineadvisor.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class WineryNeo4jRepository {

    private final Neo4jClient neo4jClient;

    public void createWinery(String username, String name, String thumbnail) {
        Map<String, Object> params = new HashMap<>();
        params.put("username", username);
        params.put("name", name);
        params.put("thumbnail", thumbnail);

        neo4jClient.query("""
            CREATE (w:Winery {
                username: $username,
                name: $name,
                thumbnail: $thumbnail
            })
        """)
        .bindAll(params)
        .run();
    }

    public Map<String, Object> findWineryByUsername(String username) {
        return neo4jClient.query("""
                MATCH (w:Winery {username: $username})
                RETURN w.username AS username, w.name AS name, w.thumbnail AS thumbnail
            """)
            .bind(username).to("username")
            .fetch()
            .one()
            .orElse(null);
    }

    public void updateWinery(String username, String name, String thumbnail) {
        Map<String, Object> params = new HashMap<>();
        params.put("username", username);
        params.put("name", name);
        params.put("thumbnail", thumbnail);

        neo4jClient.query("""
                MATCH (w:Winery {username: $username})
                SET w.name = $name,
                    w.thumbnail = $thumbnail
            """)
            .bindAll(params)
            .run();
    }

    public void updateWineryUsername(String oldUsername, String newUsername) {
        neo4jClient.query("""
            MATCH (w:Winery {username: $oldUsername})
            SET w.username = $newUsername
        """)
        .bind(oldUsername).to("oldUsername")
        .bind(newUsername).to("newUsername")
        .run();
    }

    public void deleteWineryByUsername(String username) {
        neo4jClient.query("""
            MATCH (w:Winery {username: $username})
            DETACH DELETE w
        """)
        .bind(username).to("username")
        .run();
    }

    public void deleteAllWineries() {
        neo4jClient.query("""
            MATCH (w:Winery)
            DETACH DELETE w
        """)
        .run();
    }
}
