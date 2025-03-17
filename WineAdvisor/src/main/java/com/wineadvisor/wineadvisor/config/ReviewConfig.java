package com.wineadvisor.wineadvisor.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class ReviewConfig {

    @Bean
    public MongoClient mongoClient() {
        
        MongoClient mongoClient = MongoClients.create(
                MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString("mongodb://localhost:27017"))
                .build());
        
        return mongoClient;
    }
}
