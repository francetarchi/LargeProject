package com.wineadvisor.wineadvisor.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

import java.util.stream.Collectors;
import java.util.HashMap;

// This repository handles user-related operations in Neo4j

@Repository
@RequiredArgsConstructor
public class UserNeo4jRepository {
    private final Neo4jClient neo4jClient;

    // Create a user with the given details
    public void createUser(String username, String firstName, String lastName, String thumbnail) {
        Map<String, Object> params = new HashMap<>();
        params.put("username", username);
        params.put("firstName", firstName);
        params.put("lastName", lastName);
        params.put("thumbnail", thumbnail);
        
        neo4jClient.query("""
            CREATE (u:User {
                username: $username,
                firstName: $firstName,
                lastName: $lastName,
                thumbnail: $thumbnail
            })
        """)
        .bindAll(params)
        .run();
    }

    // Find a user by their username
    public Map<String, Object> findUserByUsername(String username) {
        return neo4jClient.query("""
                MATCH (u:User {username: $username})
                RETURN u.username AS username, u.firstName AS firstName, u.lastName AS lastName, u.thumbnail AS thumbnail
            """)
            .bind(username).to("username")
            .fetch()
            .one()
            .orElse(null);
    }

    // Update user details by username
    public void updateUser(String username, String firstName, String lastName, String thumbnail) {
        Map<String, Object> params = new HashMap<>();
        params.put("username", username);
        params.put("firstName", firstName);
        params.put("lastName", lastName);
        params.put("thumbnail", thumbnail);

        neo4jClient.query("""
                MATCH (u:User {username: $username})
                SET u.firstName = $firstName,
                    u.lastName = $lastName,
                    u.thumbnail = $thumbnail
            """)
            .bindAll(params)
            .run();
    }

    // Update only the username of a user
    public void updateUserUsername(String oldUsername, String newUsername) {
        neo4jClient.query("""
            MATCH (u:User {username: $oldUsername})
            SET u.username = $newUsername
        """)
        .bind(oldUsername).to("oldUsername")
        .bind(newUsername).to("newUsername")
        .run();
    }

    // Delete a user by their username
    public void deleteUserByUsername(String username) {
        neo4jClient.query("""
            MATCH (u:User {username: $username})
            DETACH DELETE u
        """)
        .bind(username).to("username")
        .run();
    }

    // Delete all users in the database
    public void deleteAllUsers() {
        neo4jClient.query("""
            MATCH (u:User)
            DETACH DELETE u
        """)
        .run();
    }

    // Follow another user or winery
    public void follow(String followerUsername, String targetUsername, boolean isWinery) {
        String query = isWinery
            ? """
                MATCH (f:User {username: $follower})
                MATCH (w:Winery {username: $target})
                MERGE (f)-[:FOLLOWS]->(w)
            """
            : """
                MATCH (f:User {username: $follower})
                MATCH (t:User {username: $target})
                MERGE (f)-[:FOLLOWS]->(t)
            """;

        neo4jClient.query(query)
            .bind(followerUsername).to("follower")
            .bind(targetUsername).to("target")
            .run();
    }

    // Unfollow a user or winery
    public void unfollow(String followerUsername, String targetUsername) {
        neo4jClient.query("""
            MATCH (f:User {username: $follower})-[rel:FOLLOWS]->(t)
            WHERE t.username = $target
            DELETE rel
        """)
        .bind(followerUsername).to("follower")
        .bind(targetUsername).to("target")
        .run();
    }

    // Get recommended users to follow based on mutual follows
    public List<Map<String, Object>> getSuggestedFollows(String username) {
    String query = """
        MATCH (me:User {username: $username})-[:FOLLOWS]->(friend:User)
        MATCH (friend)-[:FOLLOWS]->(suggested)
        WHERE NOT (me)-[:FOLLOWS]->(suggested) AND suggested <> me
              AND (suggested:User OR suggested:Winery)
        RETURN suggested.username AS username,
               suggested.name AS name,
               suggested.thumbnail AS thumbnail,
               COUNT(*) AS mutualFollows
        ORDER BY mutualFollows DESC
        LIMIT 10
    """;


    List<Map<String, Object>> result = neo4jClient.query(query)
    .bind(username).to("username")
    .fetch()
    .all()
    .stream()
    .map(record -> {
        Map<String, Object> r = new HashMap<>();
        r.put("username", (String) record.get("username"));
        r.put("name", (String) record.get("name"));
        r.put("thumbnail", (String) record.get("thumbnail"));
        r.put("mutualFollows", ((Number) record.get("mutualFollows")).intValue());
        return r;
    })
    .collect(Collectors.toList());

System.out.println(">>> SUGGESTED: " + result);
return result;

}

public List<Map<String, Object>> getRandomFollows(String username, int limit) {
    String query = """
        MATCH (s)
        WHERE (s:User OR s:Winery)
          MATCH (me:User {username: $username})
          WHERE NOT (me)-[:FOLLOWS]->(s) AND s.username <> $username
          AND s.username <> $username
        RETURN s.username AS username,
               s.name AS name,
               s.thumbnail AS thumbnail
        ORDER BY rand()
        LIMIT $limit
    """;

    return neo4jClient.query(query)
        .bind(username).to("username")
        .bind(limit).to("limit")
        .fetch()
        .all()
        .stream()
        .map(record -> {
            Map<String, Object> r = new HashMap<>();
            r.put("username", (String) record.get("username"));
            r.put("name", (String) record.get("name"));
            r.put("thumbnail", (String) record.get("thumbnail"));
            return r;
        })
        .collect(Collectors.toList());
}


}
