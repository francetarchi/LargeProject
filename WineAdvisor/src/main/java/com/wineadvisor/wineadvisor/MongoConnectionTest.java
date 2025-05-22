package com.wineadvisor.wineadvisor; // oppure aggiusta il package se lo metti in una sottocartella

import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
public class MongoConnectionTest implements CommandLineRunner {

    private final MongoTemplate mongoTemplate;

    public MongoConnectionTest(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void run(String... args) {
        try {
            var collections = mongoTemplate.getCollectionNames();
            System.out.println("✅ Connesso a MongoDB. Collezioni trovate: " + collections);
        } catch (Exception e) {
            System.err.println("❌ Errore di connessione a MongoDB: " + e.getMessage());
        }
    }
}
