package com.wineadvisor.wineadvisor.repository_neo4j;

import lombok.RequiredArgsConstructor;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;


@Repository
@RequiredArgsConstructor
public class UserNeo4jRepository {

    private final Neo4jClient neo4jClient;

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

    public void updateUserUsername(String oldUsername, String newUsername) {
        neo4jClient.query("""
            MATCH (u:User {username: $oldUsername})
            SET u.username = $newUsername
        """)
        .bind(oldUsername).to("oldUsername")
        .bind(newUsername).to("newUsername")
        .run();
    }

    public void deleteUserByUsername(String username) {
        neo4jClient.query("""
            MATCH (u:User {username: $username})
            DETACH DELETE u
        """)
        .bind(username).to("username")
        .run();
    }

    public void deleteAllUsers() {
        neo4jClient.query("""
            MATCH (u:User)
            DETACH DELETE u
        """)
        .run();
    }
}
