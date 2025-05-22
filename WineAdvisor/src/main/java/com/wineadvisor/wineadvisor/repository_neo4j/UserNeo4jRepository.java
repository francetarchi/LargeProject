package com.wineadvisor.wineadvisor.repository_neo4j;

import lombok.RequiredArgsConstructor;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Repository;

import java.util.Map;


@Repository
@RequiredArgsConstructor
public class UserNeo4jRepository {

    private final Neo4jClient neo4jClient;

    public void createUser(String username, String firstName, String lastName, String thumbnail) {
        neo4jClient.query("""
            CREATE (u:User {
                username: $username,
                firstName: $firstName,
                lastName: $lastName,
                thumbnail: $thumbnail
            })
        """)
        .bindAll(Map.of(
            "username", username,
            "firstName", firstName,
            "lastName", lastName,
            "thumbnail", thumbnail
        ))
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
        neo4jClient.query("""
                MATCH (u:User {username: $username})
                SET u.firstName = $firstName,
                    u.lastName = $lastName,
                    u.thumbnail = $thumbnail
            """)
            .bindAll(Map.of(
                "username", username,
                "firstName", firstName,
                "lastName", lastName,
                "thumbnail", thumbnail
            ))
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
}
