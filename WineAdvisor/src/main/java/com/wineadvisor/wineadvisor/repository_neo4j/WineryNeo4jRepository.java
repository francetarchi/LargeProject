package com.wineadvisor.wineadvisor.repository_neo4j;

import lombok.RequiredArgsConstructor;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
@RequiredArgsConstructor
public class WineryNeo4jRepository {

    private final Neo4jClient neo4jClient;

    public void createWinery(String username, String name, String thumbnail) {
        neo4jClient.query("""
            CREATE (w:Winery {
                username: $username,
                name: $name,
                thumbnail: $thumbnail
            })
        """)
        .bindAll(Map.of(
            "username", username,
            "name", name,
            "thumbnail", thumbnail
        ))
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
}
