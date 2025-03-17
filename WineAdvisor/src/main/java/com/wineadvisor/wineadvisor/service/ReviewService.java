package com.wineadvisor.wineadvisor.service;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.*;

import java.util.*;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

@Service
public class ReviewService {

    private final MongoClient mongoClient;
    private final MongoDatabase database;

    public MongoDatabase getDatabase() {
        return this.database;
    }

    public ReviewService(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
        this.database = mongoClient.getDatabase("wineadvisor");
    }

    public String getDatabaseName() {
        MongoDatabase database = mongoClient.getDatabase("wineadvisor");
        return "Connesso al database: " + database.getName();
    }

    public String getCollectionName() {
        MongoCollection<Document> collection = database.getCollection("reviews"); // Accesso alla collection
        return "Connesso alla collection: " + collection.getNamespace();
    }

    // CRUD operations
    // Funzioni per l'utente
    // Aggiunge una recensione alla collection "reviews" del database
    public void addReview(String username, String thumbnail, int wineId, String wineName, int vintageYear, String wineImage, double rating, String text) {
        MongoCollection<Document> collection = database.getCollection("reviews");
    
        Document user = new Document("username", username)
                .append("thumbnail", thumbnail);
        
        Document wine = new Document("id", wineId)
                .append("name", wineName)
                .append("year", vintageYear)
                .append("image", wineImage);
        
        Document review = new Document("user_id", user)
                .append("wine_id", wine)
                .append("rating", rating)
                .append("text", text)
                .append("created_at", new java.util.Date()) // Imposta la data di creazione
                .append("likes_count", 0)
                .append("dislikes_count", 0);
    
        InsertOneResult result = collection.insertOne(review);
        if(result.wasAcknowledged()) {
            System.out.println("Recensione aggiunta.");
        } else {
            System.out.println("Errore nell'aggiunta della recensione.");
        }
    }

    // Rimuove una recensione dalla collection "reviews" del database
    public void deleteReview(ObjectId reviewId) {
        MongoCollection<Document> collection = database.getCollection("reviews");
        
        Bson filter = Filters.eq("_id", reviewId);
        DeleteResult result = collection.deleteOne(filter);
        if (result.getDeletedCount() == 0) {
            System.out.println("Recensione non trovata.");
        } else {
            System.out.println("Recensione rimossa.");
        }

    }

    // Aggiorna una recensione nella collection "reviews" del db (testo e rating)
    public void updateReview(ObjectId reviewId, String text, double rating) {
        MongoCollection<Document> collection = database.getCollection("reviews");
        
        Bson matchfilter = Filters.eq("_id", reviewId);
        Bson updatefilter = Updates.combine(
            Updates.set("text", text),
            Updates.set("rating", rating)
        );

        UpdateResult result = collection.updateOne(matchfilter, updatefilter);
        if (result.getModifiedCount() == 0) {
            System.out.println("Recensione non trovata.");
        } else {
            System.out.println("Recensione aggiornata.");
        }
    }

    // Aggiunge un like a una recensione
    public void addLike(ObjectId reviewId) {
        MongoCollection<Document> collection = database.getCollection("reviews");
        
        Bson filter = Filters.eq("_id", reviewId);
        Bson update = Updates.inc("likes_count", 1);
        
        UpdateResult result = collection.updateOne(filter, update);
        if (result.getModifiedCount() == 0) {
            System.out.println("Recensione non trovata.");
        } else {
            System.out.println("Like aggiunto alla recensione.");
        }
    }

    // Rimuove un like da una recensione
    public void removeLike(ObjectId reviewId) {
        MongoCollection<Document> collection = database.getCollection("reviews");
        
        Bson filter = Filters.eq("_id", reviewId);
        Bson update = Updates.inc("likes_count", -1);
        
        UpdateResult result = collection.updateOne(filter, update);
        if (result.getModifiedCount() == 0) {
            System.out.println("Recensione non trovata.");
        } else {
            System.out.println("Like rimosso dalla recensione.");
        }
    }

    // Aggiunge un dislike a una recensione
    public void addDislike(ObjectId reviewId) {
        MongoCollection<Document> collection = database.getCollection("reviews");
        
        Bson filter = Filters.eq("_id", reviewId);
        Bson update = Updates.inc("dislikes_count", 1);
        
        UpdateResult result = collection.updateOne(filter, update);
        if (result.getModifiedCount() == 0) {
            System.out.println("Recensione non trovata.");
        } else {
            System.out.println("Dislike aggiunto alla recensione.");
        }
    }

    // Rimuove un dislike da una recensione
    public void removeDislike(ObjectId reviewId) {
        MongoCollection<Document> collection = database.getCollection("reviews");
        
        Bson filter = Filters.eq("_id", reviewId);
        Bson update = Updates.inc("dislikes_count", -1);
        
        UpdateResult result = collection.updateOne(filter, update);
        if (result.getModifiedCount() == 0) {
            System.out.println("Recensione non trovata.");
        } else {
            System.out.println("Dislike rimosso dalla recensione.");
        }
    }

    // Funzioni di utilità e per utente root
    // Ottiene una recensione dal database
    public Document getReviewById(ObjectId reviewId) {
        MongoCollection<Document> collection = database.getCollection("reviews");
        
        Bson filter = Filters.eq("_id", reviewId);
        Document review = collection.find(filter).first();
        return review;
    }

    // Ottiene tutte le recensioni dalla collection "reviews" del database
    public List<Document> getReviews() {
        MongoCollection<Document> collection = database.getCollection("reviews");

        List<Document> reviews = collection.find().into(new ArrayList<>());
        return reviews;
    }

    // Ottiene tutte le recensioni di un vino specifico dalla collection "reviews" del database
    public List<Document> getReviewsByWine(int wineId) {
        MongoCollection<Document> collection = database.getCollection("reviews");

        Bson filter = Filters.eq("wine_id.id", wineId);
        List<Document> reviews = collection.find(filter).into(new ArrayList<>());
        return reviews;
    }

    // Ottiene tutte le recensioni di un utente specifico dalla collection "reviews" del database
    public List<Document> getReviewsByUser(String username) {
        MongoCollection<Document> collection = database.getCollection("reviews");

        Bson filter = Filters.eq("user_id.username", username);
        List<Document> reviews = collection.find(filter).into(new ArrayList<>());
        return reviews;
    }

    // Ottiene tutte le recensioni di un utente specifico per un vino specifico dalla collection "reviews" del database
    public List<Document> getReviewsByUserAndWine(String username, int wineId) {
        MongoCollection<Document> collection = database.getCollection("reviews");

        Bson filter = Filters.and(
            Filters.eq("user_id.username", username),
            Filters.eq("wine_id.id", wineId)
        );
        List<Document> reviews = collection.find(filter).into(new ArrayList<>());
        return reviews;
    }

}
