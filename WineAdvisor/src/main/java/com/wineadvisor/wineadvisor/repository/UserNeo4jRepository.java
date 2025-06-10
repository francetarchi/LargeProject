package com.wineadvisor.wineadvisor.repository;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;


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
    public List<Map<String, Object>> getUserSuggestedFollows(String username) {
        String query = """
            MATCH (me:User {username: $username})-[:FOLLOWS]->(friend:User)
            MATCH (friend)-[:FOLLOWS]->(suggested)
            WHERE NOT (me)-[:FOLLOWS]->(suggested) AND suggested <> me
                AND (suggested:User OR suggested:Winery)
            RETURN suggested.username AS username,
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
            r.put("thumbnail", (String) record.get("thumbnail"));
            r.put("mutualFollows", ((Number) record.get("mutualFollows")).intValue());
            return r;
        })
        .collect(Collectors.toList());

        System.out.println(">>> SUGGESTED: " + result);
        return result;
    }

    // Get a random list of users or wineries that the user does not follow
    public List<Map<String, Object>> getRandomFollows(String username, int limit) {
        String query = """
            MATCH (s)
            WHERE (s:User OR s:Winery)
            MATCH (me:User {username: $username})
            WHERE NOT (me)-[:FOLLOWS]->(s) AND s.username <> $username
            AND s.username <> $username
            RETURN s.username AS username,
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
                r.put("thumbnail", (String) record.get("thumbnail"));
                return r;
            })
            .collect(Collectors.toList());
    }

    // Get a list of users that follow a specific user
    public List<Map<String, Object>> getUserFollowers(String username) {
        String query = """
            MATCH (follower:User)-[:FOLLOWS]->(u:User {username: $username})
            RETURN follower.username AS username, follower.thumbnail AS thumbnail
        """;

        return new ArrayList<>(
            neo4jClient.query(query)
                .bind(username).to("username")
                .fetch()
                .all()
        );
    }

    // Get a list of users that a specific user follows
    public List<Map<String, Object>> getUserFollowing(String username) {
        String query = """
            MATCH (u:User {username: $username})-[:FOLLOWS]->(followed)
            RETURN followed.username AS username,
                followed.thumbnail AS thumbnail
        """;

        return neo4jClient.query(query)
                .bind(username).to("username")
                .fetch()
                .all()
                .stream()
                .map(record -> {
                    Map<String, Object> r = new HashMap<>();
                    r.put("username", record.get("username"));
                    r.put("thumbnail", record.get("thumbnail"));
                    return r;
                })
                .collect(Collectors.toList());
    }
}
