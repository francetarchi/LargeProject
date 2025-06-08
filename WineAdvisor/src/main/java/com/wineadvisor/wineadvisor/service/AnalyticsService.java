package com.wineadvisor.wineadvisor.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wineadvisor.wineadvisor.exception.BadRequestException;
import com.wineadvisor.wineadvisor.exception.ResourceNotFoundException;
import com.wineadvisor.wineadvisor.model.analytics.TopVintagesOurQopType;
import com.wineadvisor.wineadvisor.model.analytics.TopVintagesQopType;
import com.wineadvisor.wineadvisor.model.analytics.TopVintagesRatingsType;
import com.wineadvisor.wineadvisor.model.analytics.TopWineriesRatings;
import com.wineadvisor.wineadvisor.model.analytics.TopWinesRatingsType;
import com.wineadvisor.wineadvisor.model.countries.Country;
import com.wineadvisor.wineadvisor.model.regions.Region;
import com.wineadvisor.wineadvisor.model.users.User;
import com.wineadvisor.wineadvisor.model.utils.VintageEmbedded;
import com.wineadvisor.wineadvisor.model.wineries.Winery;
import com.wineadvisor.wineadvisor.repository.CountryRepository;
import com.wineadvisor.wineadvisor.repository.RegionRepository;
import com.wineadvisor.wineadvisor.repository.UserRepository;
import com.wineadvisor.wineadvisor.repository.WineryRepository;
import com.wineadvisor.wineadvisor.repository.analytics.TopVintagesOurQopTypeRepository;
import com.wineadvisor.wineadvisor.repository.analytics.TopVintagesQopTypeRepository;
import com.wineadvisor.wineadvisor.repository.analytics.TopVintagesRatingsTypeRepository;
import com.wineadvisor.wineadvisor.repository.analytics.TopWineriesRatingsRepository;
import com.wineadvisor.wineadvisor.repository.analytics.TopWinesRatingsTypeRepository;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class AnalyticsService {
    ////////////////////////////////
    /////////// VARIABLES //////////
    ////////////////////////////////
    private final TopVintagesOurQopTypeRepository topVintagesOurQopTypeRepository;
    private final TopVintagesQopTypeRepository topVintagesQopTypeRepository;
    private final TopVintagesRatingsTypeRepository topVintagesRatingsTypeRepository;
    private final TopWinesRatingsTypeRepository topWinesRatingsTypeRepository;
    private final TopWineriesRatingsRepository topWineriesRatingsRepository;
    private final RegionRepository regionRepository;
    private final CountryRepository countryRepository;
    private final UserRepository userRepository;
    private final WineryRepository wineryRepository;

    private final MongoTemplate mongoTemplate;
    
    /////////// COSTANTI ////////////
    private static final String TOP_VINTAGES_OUR_QOP = "top_vintages_by_our_qop_per_type";
    private static final String TOP_VINTAGES_QOP = "top_vintages_by_qop_per_type";
    private static final String TOP_VINTAGES_RATINGS = "top_vintages_by_ratings_per_type";
    private static final String TOP_WINES_RATINGS = "top_wines_by_ratings_per_type";
    private static final String TOP_WINERIES_RATINGS = "top_wineries_by_wines_ratings";

    private static final Integer TOP_LENGTH = 10; // Lunghezza attuale di default delle classifiche

    private static final int PAGE_SIZE = 20;



    ////////////////////////////////
    /////// PRIVATE METHODS ////////
    ////////////////////////////////
    
    ////////////////////////////////
    ///// Checking operations //////

    // Controlla che il tipo di vino sia valido (rossi, bianchi, rosati...).
    private String checkType(String type) {
        type = type.toLowerCase();

        if (!type.equalsIgnoreCase("rosso")
            && !type.equalsIgnoreCase("bianco")
            && !type.equalsIgnoreCase("rosato")
            && !type.equalsIgnoreCase("spumante")
            && !type.equalsIgnoreCase("vino da dessert")
            && !type.equalsIgnoreCase("vino liquoroso")
            && !type.equalsIgnoreCase("vino aromatizzato")
        ) {
            throw new BadRequestException("Invalid type: \"" + type + "\". Valid types are: rosso, bianco, rosato, spumante, vino da dessert, vino liquoroso, vino aromatizzato.");
        }

        return type;
    }

    /// END of checking operations ///
    //////////////////////////////////
    

    //////////////////////////////////
    ///// Creation of mat. views ///// (functions called by the respective scheduled methods which update the materialized views)
    
    // Crea la meterialized view "top_vintages_by_our_qop_per_type" che contiene le migliori annate per rapporto qualità/prezzo (calcolati secondo la nostra formula), se non esiste già.
    // Assicura l'esistenza di un indice UNIQUE sul campo "type" (è il campo utilizzato per lo stage finale della pipeline: se manca, l'aggiornamento fallisce).
    private void createTopVintagesByOurQoPPerType() {
        if (!mongoTemplate.collectionExists(TOP_VINTAGES_OUR_QOP)) {
            mongoTemplate.createCollection(TOP_VINTAGES_OUR_QOP);
            System.out.println("--- INFO: Materialized view \"" + TOP_VINTAGES_OUR_QOP + "\" created successfully.");
        } else {
            System.out.println("--- INFO: Materialized view \"" + TOP_VINTAGES_OUR_QOP + "\" already exists.");
        }

        // Assicuro che esistano gli indici NECESSARI alla pipeline.
        mongoTemplate
            .indexOps(TOP_VINTAGES_OUR_QOP)
            .ensureIndex(
                new Index()
                    .on("type", Sort.Direction.ASC)
                    .named("on_type_UNIQUE")
                    .unique()
            );
        System.out.println("--- INFO: Ensured UNIQUE index on field \"type\" for materialized view \"" + TOP_VINTAGES_OUR_QOP + "\".");
    }

    // Crea la meterialized view "top_vintages_by_qop_per_type" che contiene le migliori annate per rapporto qualità/prezzo (calcolati secondo la formula "base"), se non esiste già.
    // Assicura l'esistenza di un indice UNIQUE sul campo "type" (è il campo utilizzato per lo stage finale della pipeline: se manca, l'aggiornamento fallisce).
    private void createTopVintagesByQoPPerType() {
        if (!mongoTemplate.collectionExists(TOP_VINTAGES_QOP)) {
            mongoTemplate.createCollection(TOP_VINTAGES_QOP);
            System.out.println("--- INFO: Materialized view \"" + TOP_VINTAGES_QOP + "\" created successfully.");
        } else {
            System.out.println("--- INFO: Materialized view \"" + TOP_VINTAGES_QOP + "\" already exists.");
        }

        // Assicuro che esistano gli indici NECESSARI alla pipeline.
        mongoTemplate
            .indexOps(TOP_VINTAGES_QOP)
            .ensureIndex(
                new Index()
                    .on("type", Sort.Direction.ASC)
                    .named("on_type_UNIQUE")
                    .unique()
            );
        System.out.println("--- INFO: Ensured UNIQUE index on field \"type\" for materialized view \"" + TOP_VINTAGES_QOP + "\".");
    }

    // Crea la meterialized view "top_vintages_by_ratings_per_type" che contiene le migliori annate per valutazione media, se non esiste già.
    // Assicura l'esistenza di un indice UNIQUE sul campo "type" (è il campo utilizzato per lo stage finale della pipeline: se manca, l'aggiornamento fallisce).
    private void createTopVintagesByRatingsPerType() {
        if (!mongoTemplate.collectionExists(TOP_VINTAGES_RATINGS)) {
            mongoTemplate.createCollection(TOP_VINTAGES_RATINGS);
            System.out.println("--- INFO: Materialized view \"" + TOP_VINTAGES_RATINGS + "\" created successfully.");
        } else {
            System.out.println("--- INFO: Materialized view \"" + TOP_VINTAGES_RATINGS + "\" already exists.");
        }

        // Assicuro che esistano gli indici NECESSARI alla pipeline.
        mongoTemplate
            .indexOps(TOP_VINTAGES_RATINGS)
            .ensureIndex(
                new Index()
                    .on("type", Sort.Direction.ASC)
                    .named("on_type_UNIQUE")
                    .unique()
            );
        System.out.println("--- INFO: Ensured UNIQUE index on field \"type\" for materialized view \"" + TOP_VINTAGES_RATINGS + "\".");
    }

    // Crea la meterialized view "top_wines_by_ratings_per_type" che contiene i migliori vini per valutazione media, se non esiste già.
    // Assicura l'esistenza di un indice UNIQUE sul campo "type" (è il campo utilizzato per lo stage finale della pipeline: se manca, l'aggiornamento fallisce).
    private void createTopWinesByRatingsPerType() {
        if (!mongoTemplate.collectionExists(TOP_WINES_RATINGS)) {
            mongoTemplate.createCollection(TOP_WINES_RATINGS);
            System.out.println("--- INFO: Materialized view \"" + TOP_WINES_RATINGS + "\" created successfully.");
        } else {
            System.out.println("--- INFO: Materialized view \"" + TOP_WINES_RATINGS + "\" already exists.");
        }

        // Assicuro che esistano gli indici NECESSARI alla pipeline.
        mongoTemplate
            .indexOps(TOP_WINES_RATINGS)
            .ensureIndex(
                new Index()
                    .on("type", Sort.Direction.ASC)
                    .named("on_type_UNIQUE")
                    .unique()
            );
        System.out.println("--- INFO: Ensured UNIQUE index on field \"type\" for materialized view \"" + TOP_WINES_RATINGS + "\".");
    }

    // Crea la meterialized view "top_wineries_by_wines_ratings" che contiene le migliori cantine per valutazione media dei propri vini, se non esiste già.
    // Assicura l'esistenza di un indice UNIQUE sul campo "winery_username" (è il campo utilizzato per lo stage finale della pipeline: se manca, l'aggiornamento fallisce).
    // Assicura l'esistenza di un indice UNIQUE sul campo "login.username" della collezione "wineries" (è il campo utilizzato per il join con la collezione "wineries": se manca, la pipeline è molto più pesante e lenta).
    private void createTopWineriesByWineRatings() {
        if (!mongoTemplate.collectionExists(TOP_WINERIES_RATINGS)) {
            mongoTemplate.createCollection(TOP_WINERIES_RATINGS);
            System.out.println("--- INFO: Materialized view \"" + TOP_WINERIES_RATINGS + "\" created successfully.");
        } else {
            System.out.println("--- INFO: Materialized view \"" + TOP_WINERIES_RATINGS + "\" already exists.");
        }

        // Assicuro che esistano gli indici NECESSARI alla pipeline.
        mongoTemplate
            .indexOps(TOP_WINERIES_RATINGS)
            .ensureIndex(
                new Index()
                    .on("winery_username", Sort.Direction.ASC)
                    .named("on_winery_username_UNIQUE")
                    .unique()
            );
        System.out.println("--- INFO: Ensured UNIQUE index on field \"winery_username\" for materialized view \"" + TOP_WINERIES_RATINGS + "\".");

        // Assicuro che esistano gli indici FUNZIONALI alla pipeline.
        mongoTemplate
            .indexOps("wineries")
            .ensureIndex(
                new Index()
                    .on("login.username", Sort.Direction.ASC)
                    .named("on_username_UNIQUE")
                    .unique()
            );
        System.out.println("--- INFO: Ensured UNIQUE index on field \"login.username\" for collection \"wineries\".");
    }
    
    /// END of creation of mat. views ///
    /////////////////////////////////////
    

    /////////////////////////////////////
    /////// Aggregation pipelines /////// (scheduled functions to update the materialized views)

    // Una volta al mese, aggiorna la top vintages per qualità/prezzo, utilizzando la nostra formula.
    // Viene calcolata una classifica per ogni tipologia di vino (rossi, bianchi, rosati...).
    // Il risultato viene memorizzato nel db nella "materialized view" chiamata "top_vintages_by_our_qop_per_type"
    // @Scheduled(cron = "00 53 21 * * ?")     // Scheduling for debugging purposes
    @Scheduled(cron = "0 0 2 * * MON")      // Ogni lunedì alle 2 di notte
    protected void updateTopVintagesByOurQoPPerType() {
        // Defining the name of the starting collection
        final String SOURCE_COLLECTION_NAME = "wines";

        // Instantiating the aggregation pipeline
        System.out.println("--- INFO: Declaring aggregation pipeline stages for materialized view \"" + TOP_VINTAGES_OUR_QOP + "\".");
        List<Document> pipeline = Arrays.asList(
                //// Stage 1: Unwinding vintages
                new Document("$unwind",
                        new Document("path", "$vintages")),
                
                //// Stage 2: Projecting only necessary fields
                new Document("$project",
                        new Document("name", 1L)
                                .append("type", 1L)
                                .append("winery", "$winery.name")
                                .append("year", "$vintages.year")
                                .append("image", "$vintages.image")
                                .append("ratings_count", "$vintages.statistics.ratings_count")
                                .append("quality", "$vintages.statistics.ratings_average")
                                .append("price", "$vintages.price")
                                .append("qop",
                                        new Document("$divide",
                                                Arrays.asList(new Document("$multiply", Arrays.asList(
                                                        new Document("$exp", "$vintages.statistics.ratings_average"),
                                                        new Document("$sqrt", "$vintages.statistics.ratings_count"))),
                                                        new Document("$ln", "$vintages.price"))))),
                
                //// Stage 3: Sorting by qop (quality/price, computed with our formula) and ratings_count
                new Document("$sort",
                        new Document("qop", -1L)
                                .append("ratings_count", -1L)),

                //// Stage 4: Grouping by type to calculate max_qop for each type
                new Document("$group",
                        new Document("_id", "$type")
                                .append("max_qop",
                                        new Document("$max", "$qop"))
                                .append("vintages",
                                        new Document("$push",
                                                new Document("wine", "$name")
                                                        .append("winery", "$winery")
                                                        .append("year", "$year")
                                                        .append("image", "$image")
                                                        .append("ratings_count", "$ratings_count")
                                                        .append("quality", "$quality")
                                                        .append("price", "$price")
                                                        .append("qop", "$qop")))),

                //// Stage 5: Unwinding vintages again to assign max_qop to each vintage (for further computations)
                new Document("$unwind",
                        new Document("path", "$vintages")),

                //// Stage 6: Adding "points" field (calculated as percentage of max_qop for each type)
                new Document("$addFields",
                        new Document("vintages.points",
                                new Document("$round", Arrays.asList(new Document("$multiply", Arrays.asList(100L,
                                        new Document("$divide", Arrays.asList("$vintages.qop", "$max_qop")))), 1L)))),

                //// Stage 7: Grouping by _id (it's the actual type of the wine) and pushing vintages into an array, to separate them again by type
                new Document("$group",
                        new Document("_id", "$_id")
                                .append("vintages",
                                        new Document("$push",
                                                new Document("wine", "$vintages.wine")
                                                        .append("winery", "$vintages.winery")
                                                        .append("year", "$vintages.year")
                                                        .append("image", "$vintages.image")
                                                        .append("ratings_count", "$vintages.ratings_count")
                                                        .append("quality", "$vintages.quality")
                                                        .append("price", "$vintages.price")
                                                        .append("points", "$vintages.points")))),
                
                //// Stage 8: Projecting final fields
                new Document("$project",
                        new Document("_id", 0L)
                                .append("type", "$_id")
                                .append("vintages",
                                        new Document("$slice", Arrays.asList("$vintages", 1000L)))),
                
                //// Stage 9: Merging the results into the materialized view
                new Document("$merge",
                    new Document("into", TOP_VINTAGES_OUR_QOP)
                            .append("on", "type")
                            // se un documento con lo stesso "type" esiste già nella materialized view, lo rimpiazzo
                            .append("whenMatched", "replace")
                            // se non esiste, lo inserisco
                            .append("whenNotMatched", "insert"))
        );

        // Creating the materialized view
        System.out.println("--- INFO: Creating empty materialized view \"" + TOP_VINTAGES_OUR_QOP + "\".");
        createTopVintagesByOurQoPPerType();
        
        // Executing the pipeline
        System.out.println("--- INFO: Executing aggregation pipeline for materialized view \"" + TOP_VINTAGES_OUR_QOP + "\"...");
        mongoTemplate.getCollection(SOURCE_COLLECTION_NAME).aggregate(pipeline).allowDiskUse(true).toCollection();

        System.out.println("--- INFO: Materialized view \"" + TOP_VINTAGES_OUR_QOP + "\" updated successfully.\n\n");
    }

    // Una volta al mese, aggiorna la top vintages per qualità/prezzo, utilizzando la formula "base" (ratings_average/price).
    // Viene calcolata una classifica per ogni tipologia di vino (rossi, bianchi, rosati...).
    // Il risultato viene memorizzato nel db nella "materialized view" chiamata "top_vintages_by_qop_per_type"
    // @Scheduled(cron = "00 53 21 * * ?")     // Scheduling for debugging purposes
    @Scheduled(cron = "0 0 2 * * MON")      // Ogni lunedì alle 2 di notte
    protected void updateTopVintagesByQoPPerType() {
        // Defining the name of the starting collection
        final String SOURCE_COLLECTION_NAME = "wines";

        // Instantiating the aggregation pipeline
        System.out.println("--- INFO: Declaring aggregation pipeline stages for materialized view \"" + TOP_VINTAGES_QOP + "\".");
        List<Document> pipeline = Arrays.asList(
                //// Stage 1: Unwinding vintages
                new Document("$unwind",
                        new Document("path", "$vintages")),
                
                //// Stage 2: Projecting only necessary fields
                new Document("$project",
                        new Document("name", 1L)
                                .append("type", 1L)
                                .append("winery", "$winery.name")
                                .append("year", "$vintages.year")
                                .append("image", "$vintages.image")
                                .append("ratings_count", "$vintages.statistics.ratings_count")
                                .append("quality", "$vintages.statistics.ratings_average")
                                .append("price", "$vintages.price")
                                .append("qop",
                                        new Document("$divide", Arrays.asList("$vintages.statistics.ratings_average", "$vintages.price")))),
                
                //// Stage 3: Sorting by qop (quality/price, computed with our formula) and ratings_count
                new Document("$sort",
                        new Document("qop", -1L)
                                .append("ratings_count", -1L)),

                //// Stage 4: Grouping by type to calculate max_qop for each type
                new Document("$group",
                        new Document("_id", "$type")
                                .append("max_qop",
                                        new Document("$max", "$qop"))
                                .append("vintages",
                                        new Document("$push",
                                                new Document("wine", "$name")
                                                        .append("winery", "$winery")
                                                        .append("year", "$year")
                                                        .append("image", "$image")
                                                        .append("ratings_count", "$ratings_count")
                                                        .append("quality", "$quality")
                                                        .append("price", "$price")
                                                        .append("qop", "$qop")))),

                //// Stage 5: Unwinding vintages again to assign max_qop to each vintage (for further computations)
                new Document("$unwind",
                        new Document("path", "$vintages")),

                //// Stage 6: Adding "points" field (calculated as percentage of max_qop for each type)
                new Document("$addFields",
                        new Document("vintages.points",
                                new Document("$round", Arrays.asList(new Document("$multiply", Arrays.asList(100L,
                                        new Document("$divide", Arrays.asList("$vintages.qop", "$max_qop")))), 1L)))),

                //// Stage 7: Grouping by _id (it's the actual type of the wine) and pushing vintages into an array, to separate them again by type
                new Document("$group",
                        new Document("_id", "$_id")
                                .append("vintages",
                                        new Document("$push",
                                                new Document("wine", "$vintages.wine")
                                                        .append("winery", "$vintages.winery")
                                                        .append("year", "$vintages.year")
                                                        .append("image", "$vintages.image")
                                                        .append("ratings_count", "$vintages.ratings_count")
                                                        .append("quality", "$vintages.quality")
                                                        .append("price", "$vintages.price")
                                                        .append("points", "$vintages.points")))),
                
                //// Stage 8: Projecting final fields
                new Document("$project",
                        new Document("_id", 0L)
                                .append("type", "$_id")
                                .append("vintages",
                                        new Document("$slice", Arrays.asList("$vintages", 1000L)))),
                
                //// Stage 9: Merging the results into the materialized view
                new Document("$merge",
                    new Document("into", TOP_VINTAGES_QOP)
                            .append("on", "type")
                            // se un documento con lo stesso "type" esiste già nella materialized view, lo rimpiazzo
                            .append("whenMatched", "replace")
                            // se non esiste, lo inserisco
                            .append("whenNotMatched", "insert"))
        );

        // Creating the materialized view
        System.out.println("--- INFO: Creating empty materialized view \"" + TOP_VINTAGES_QOP + "\".");
        createTopVintagesByQoPPerType();
        
        // Executing the pipeline
        System.out.println("--- INFO: Executing aggregation pipeline for materialized view \"" + TOP_VINTAGES_QOP + "\"...");
        mongoTemplate.getCollection(SOURCE_COLLECTION_NAME).aggregate(pipeline).allowDiskUse(true).toCollection();

        System.out.println("--- INFO: Materialized view \"" + TOP_VINTAGES_QOP + "\" updated successfully.\n\n");
    }

    // Una volta al mese, aggiorna la top vintages per valutazione media.
    // Viene calcolata una classifica per ogni tipologia di vino (rossi, bianchi, rosati...).
    // Il risultato viene memorizzato nel db nella "materialized view" chiamata "top_vintages_by_ratings_per_type"
    // @Scheduled(cron = "00 53 21 * * ?")     // Scheduling for debugging purposes
    @Scheduled(cron = "0 0 2 * * MON")      // Ogni lunedì alle 2 di notte
    protected void updateTopVintagesByRatingsPerType() {
        // Defining the name of the starting collection
        final String SOURCE_COLLECTION_NAME = "wines";

        // Instantiating the aggregation pipeline
        System.out.println("--- INFO: Declaring aggregation pipeline stages for materialized view \"" + TOP_VINTAGES_RATINGS + "\".");
        List<Document> pipeline = Arrays.asList(
                //// Stage 1: Unwinding vintages
                new Document("$unwind",
                        new Document("path", "$vintages")),

                //// Stage 2: Projecting only necessary fields
                new Document("$project",
                        new Document("name", 1L)
                                .append("type", 1L)
                                .append("winery", "$winery.name")
                                .append("year", "$vintages.year")
                                .append("price", "$vintages.price")
                                .append("image", "$vintages.image")
                                .append("ratings_average", "$vintages.statistics.ratings_average")
                                .append("ratings_count", "$vintages.statistics.ratings_count")),

                //// Stage 3: Sorting by ratings_average and ratings_count
                new Document("$sort",
                        new Document("ratings_average", -1L)
                                .append("ratings_count", -1L)),
                                
                //// Stage 4: Grouping by type to create top vintages for each type
                new Document("$group",
                        new Document("_id", "$type")
                                .append("vintages",
                                        new Document("$push",
                                                new Document("wine", "$name")
                                                        .append("winery", "$winery")
                                                        .append("year", "$year")
                                                        .append("price", "$price")
                                                        .append("image", "$image")
                                                        .append("ratings_average", "$ratings_average")
                                                        .append("ratings_count", "$ratings_count")))),
                                                        
                //// Stage 5: Projecting final fields
                new Document("$project",
                        new Document("_id", 0L)
                                .append("type", "$_id")
                                .append("vintages", 
                                        new Document("$slice", Arrays.asList("$vintages", 1000L)))), 
                                
                //// Stage 6: Merging the results into the materialized view
                new Document("$merge",
                        new Document("into", TOP_VINTAGES_RATINGS)
                                .append("on", "type")
                                .append("whenMatched", "replace")
                                .append("whenNotMatched", "insert"))
        );

        // Creating the materialized view
        System.out.println("--- INFO: Creating empty materialized view \"" + TOP_VINTAGES_RATINGS + "\".");
        createTopVintagesByRatingsPerType();
        
        // Executing the pipeline
        System.out.println("--- INFO: Executing aggregation pipeline for materialized view \"" + TOP_VINTAGES_RATINGS + "\"...");
        mongoTemplate.getCollection(SOURCE_COLLECTION_NAME).aggregate(pipeline).allowDiskUse(true).toCollection();

        System.out.println("--- INFO: Materialized view \"" + TOP_VINTAGES_RATINGS + "\" updated successfully.\n\n");
    }

    // Una volta al mese, aggiorna la top wines per valutazione media.
    // Viene calcolata una classifica per ogni tipologia di vino (rossi, bianchi, rosati...).
    // Il risultato viene memorizzato nel db nella "materialized view" chiamata "top_wines_by_ratings_per_type"
    // @Scheduled(cron = "00 53 21 * * ?")     // Scheduling for debugging purposes
    @Scheduled(cron = "0 0 2 * * MON")      // Ogni lunedì alle 2 di notte
    protected void updateTopWinesByRatingsPerType() {
        // Defining the name of the starting collection
        final String SOURCE_COLLECTION_NAME = "wines";

        // Instantiating the aggregation pipeline
        System.out.println("--- INFO: Declaring aggregation pipeline stages for materialized view \"" + TOP_WINES_RATINGS + "\".");
        List<Document> pipeline = Arrays.asList(
                //// Stage 1: Projecting only necessary fields
                new Document("$project",
                        new Document("name", 1L)
                                .append("type", 1L)
                                .append("winery", "$winery.name")
                                .append("prices_average",   // dato che i prezzi sono diversi per ogni vintage, calcolo la media dei prezzi delle annate
                                        new Document("$round",
                                                Arrays.asList(new Document("$avg", "$vintages.price"), 2L)))
                                .append("image",
                                        new Document("$arrayElemAt", Arrays.asList("$vintages.image", 0L)))
                                .append("ratings_average", "$statistics.ratings_average")
                                .append("ratings_count", "$statistics.ratings_count")),

                //// Stage 2: Sorting by ratings_average and ratings_count
                new Document("$sort",
                        new Document("ratings_average", -1L)
                                .append("ratings_count", -1L)),

                //// Stage 3: Grouping by type to create top wines for each type
                new Document("$group",
                        new Document("_id", "$type")
                                .append("wines",
                                        new Document("$push",
                                                new Document("wine", "$name")
                                                        .append("winery", "$winery")
                                                        .append("image", "$image")
                                                        .append("prices_average", "$prices_average")
                                                        .append("ratings_average", "$ratings_average")
                                                        .append("ratings_count", "$ratings_count")))),

                //// Stage 4: Projecting final fields
                new Document("$project",
                        new Document("_id", 0L)
                                .append("type", "$_id")
                                .append("wines", 
                                        new Document("$slice", Arrays.asList("$wines", 1000L)))), 

                //// Stage 5: Merging the results into the materialized view
                new Document("$merge",
                        new Document("into", TOP_WINES_RATINGS)
                                .append("on", "type")
                                .append("whenMatched", "replace")
                                .append("whenNotMatched", "insert"))
        );

        // Creating the materialized view
        System.out.println("--- INFO: Creating empty materialized view \"" + TOP_WINES_RATINGS + "\"...");
        createTopWinesByRatingsPerType();
        
        // Executing the pipeline
        System.out.println("--- INFO: Executing aggregation pipeline for materialized view \"" + TOP_WINES_RATINGS + "\"...");
        mongoTemplate.getCollection(SOURCE_COLLECTION_NAME).aggregate(pipeline).allowDiskUse(true).toCollection();

        System.out.println("--- INFO: Materialized view \"" + TOP_WINES_RATINGS + "\" updated successfully.\n\n");
    }

    // Una volta al mese, aggiorna la top wineries per valutazione media dei propri vini.
    // Viene calcolata una classifica unica, senza tener conto (come avviene invece per le altre classifiche) della suddivisione per tipologia di vino (rossi, bianchi, rosati...).
    // Il risultato viene memorizzato nel db nella "materialized view" chiamata "top_wineries_by_wines_ratings"
    // @Scheduled(cron = "00 53 21 * * ?")     // Scheduling for debugging purposes
    @Scheduled(cron = "0 0 2 * * MON")      // Ogni lunedì alle 2 di notte
    protected void updateTopWineriesByWineRatings() {
        // Defining the name of the starting collection
        final String SOURCE_COLLECTION_NAME = "wines";

        // Instantiating the aggregation pipeline
        System.out.println("--- INFO: Declaring aggregation pipeline stages for materialized view \"" + TOP_WINERIES_RATINGS + "\".");
        List<Document> pipeline = Arrays.asList(
                //// Stage 1: Grouping by winery to calculate ratings average and total ratings count
                new Document("$group",
                        new Document("_id", "$winery.username")
                                .append("ratings_average",
                                        new Document("$avg", "$statistics.ratings_average"))
                                .append("ratings_count",
                                        new Document("$sum", "$statistics.ratings_count"))),

                //// Stage 2: Joining with wineries collection to get winery details
                new Document("$lookup",
                        new Document("from", "wineries")
                                .append("localField", "_id")
                                .append("foreignField", "login.username")
                                .append("as", "winery")),

                //// Stage 3: Unwinding the winery array to transform the array (of a single element) returned by the lookup into a single document
                new Document("$unwind",
                        new Document("path", "$winery")),

                //// Stage 4: Sorting by ratings_average and ratings_count
                new Document("$sort",
                        new Document("ratings_average", -1L)
                                .append("ratings_count", -1L)),

                //// Stage 5: Projecting final fields
                new Document("$project",
                        new Document("_id", 0L)
                                .append("winery_username", "$winery.login.username")
                                .append("thumbnail", "$winery.picture.thumbnail")
                                .append("winery", "$winery.name")
                                .append("region", "$winery.region")
                                .append("country", "$winery.country")
                                .append("ratings_average",
                                        new Document("$round", Arrays.asList("$ratings_average", 1L)))
                                .append("ratings_count", "$ratings_count")),

                //// Stage 6: Merging the results into the materialized view
                new Document("$merge",
                        new Document("into", TOP_WINERIES_RATINGS)
                                .append("on", "winery_username")
                                .append("whenMatched", "replace")
                                .append("whenNotMatched", "insert"))
        );

        // Creating the materialized view
        System.out.println("--- INFO: Creating empty materialized view \"" + TOP_WINERIES_RATINGS + "\".");
        createTopWineriesByWineRatings();
        
        // Executing the pipeline
        System.out.println("--- INFO: Executing aggregation pipeline for materialized view \"" + TOP_WINERIES_RATINGS + "\"...");
        mongoTemplate.getCollection(SOURCE_COLLECTION_NAME).aggregate(pipeline).allowDiskUse(true).toCollection();

        System.out.println("--- INFO: Materialized view \"" + TOP_WINERIES_RATINGS + "\" updated successfully.\n\n");
    }

    /// END of aggregation pipelines ///
    ////////////////////////////////////



    /////////////////////////////////
    //////// PUBLIC METHODS /////////
    /////////////////////////////////
    
    /////////////////////////////////
    /////// CRUD operations ///////// (just reading and deleting on materialized views)

    /// READ operations ///
    // Restituisce la top TOP_LENGTH vintages per qualità/prezzo (calcolati secondo la nostra formula) per la tipologia di vino richiesta come parametro (rossi, bianchi, rosati...).
    public TopVintagesOurQopType getTopVintagesByOurQopPerType(String type, Double maxPrice) {
        TopVintagesOurQopType topVintagesOurQopType = topVintagesOurQopTypeRepository.findByTypeLimitN(checkType(type), TOP_LENGTH, maxPrice).orElseThrow(
            () -> new ResourceNotFoundException("Top vintages by our qop for type: \"" + type + "\" not found.")
        );

        return topVintagesOurQopType;
    }

    // Restituisce la top TOP_LENGTH vintages per qualità/prezzo (calcolati secondo la formula "base") per la tipologia di vino richiesta come parametro (rossi, bianchi, rosati...).
    public TopVintagesQopType getTopVintagesByQopPerType(String type, Double maxPrice) {
        TopVintagesQopType topVintagesQopType = topVintagesQopTypeRepository.findByTypeLimitN(checkType(type), TOP_LENGTH, maxPrice).orElseThrow(
            () -> new ResourceNotFoundException("Top vintages by qop for type: \"" + type + "\" not found.")
        );

        return topVintagesQopType;
    }

    // Restituisce la top TOP_LENGTH vintages per valutazione media per la tipologia di vino richiesta come parametro (rossi, bianchi, rosati...).
    public TopVintagesRatingsType getTopVintagesByRatingsPerType(String type, Double maxPrice) {
        TopVintagesRatingsType topVintagesRatingsType = topVintagesRatingsTypeRepository.findByTypeLimitN(checkType(type), TOP_LENGTH, maxPrice).orElseThrow(
            () -> new ResourceNotFoundException("Top vintages by ratings for type: \"" + type + "\" not found.")
        );

        return topVintagesRatingsType;
    }

    // Restituisce la top TOP_LENGTH wines per valutazione media per la tipologia di vino richiesta come parametro (rossi, bianchi, rosati...).
    public TopWinesRatingsType getTopWinesByRatingsPerType(String type, Double maxPrice) {
        TopWinesRatingsType topWinesRatingsType = topWinesRatingsTypeRepository.findByTypeLimitN(checkType(type), TOP_LENGTH, maxPrice).orElseThrow(
            () -> new ResourceNotFoundException("Top wines by ratings for type: \"" + type + "\" not found.")
        );

        return topWinesRatingsType;
    }

    // Restituisce la top 100 wineries per valutazione media dei propri vini, senza tener conto della tipologia di vino.
    public List<TopWineriesRatings> getTopWineriesByWinesRatings() {
        List<TopWineriesRatings> topWineriesRatings = topWineriesRatingsRepository.findFirst100By();
        if (topWineriesRatings.isEmpty()) {
            throw new ResourceNotFoundException("No top wineries found.");
        }

        return topWineriesRatings;
    }

    // Restituisce la top 10 vintages of the month della region dell'utente, (aggregation) contenuta in regions
    public ArrayList<VintageEmbedded> getTop10VintagesOfTheWeek(String username) {
        Optional<User> user_to_find = userRepository.findByLogin_Username(username);
        String region_name = "";
        
        if(user_to_find.isEmpty()){
            Winery winery = wineryRepository.findByLogin_Username(username)
                .orElseThrow(() -> new ResourceNotFoundException("User " + username + " not found."));
            region_name = winery.getRegion();   
        } else {
            if(user_to_find.isEmpty()){
                throw new ResourceNotFoundException("User " + username + " not found.");
            }
            User user = user_to_find.get();
            region_name = user.getAddress().getRegion();
        }

        Region region  = regionRepository.findByName(region_name)
                .orElseThrow(() -> new ResourceNotFoundException("Region not found."));

        ArrayList<VintageEmbedded> vintages = region.getTop10VintagesOfTheWeek();

        return vintages;
    }

    // Restituisce la top 100 vintages of the month della country dell'utente, (aggregation) contenuta in countries
    public Page<VintageEmbedded> getTop100VintagesOfTheWeek(String username, Integer page) {
        Optional<User> user_to_find = userRepository.findByLogin_Username(username);
        String country_name = "";
        
        if(user_to_find.isEmpty()){
            Winery winery = wineryRepository.findByLogin_Username(username)
                .orElseThrow(() -> new ResourceNotFoundException("User " + username + " not found."));
            country_name = winery.getCountry();
        } else {
            if(user_to_find.isEmpty()){
                throw new ResourceNotFoundException("User " + username + " not found.");
            }
            User user = user_to_find.get();
            country_name = user.getAddress().getCountry();
        }

        Country country = countryRepository.findByName(country_name)
                .orElseThrow(() -> new ResourceNotFoundException("Country not found."));

        ArrayList<VintageEmbedded> vintages = country.getTop100VintagesOfTheWeek();

        Pageable pageable = PageRequest.of(page, PAGE_SIZE);

        // Calcolo degli indici di inizio e fine pagina
        int start = Math.min((int) pageable.getOffset(), vintages.size());
        int end = Math.min(start + pageable.getPageSize(), vintages.size());

        List<VintageEmbedded> paginatedList = vintages.subList(start, end);

        return new PageImpl<>(paginatedList, pageable, vintages.size());
    }


    /// DELETE operations ///
    // Elimina tutti i documenti dalla top vintages per qualità/prezzo (calcolati secondo la nostra formula) per tipologia di vino.
    @Transactional
    public void deleteTopVintagesByOurQopPerType() {
        topVintagesOurQopTypeRepository.deleteAll();
    }

    // Elimina il documento di un certo tipo di vino dalla top vintages per qualità/prezzo (calcolati secondo la nostra formula) per tipologia di vino.
    public void deleteTopVintagesByOurQopPerTypeByType(String type) {
        TopVintagesOurQopType topVintagesOurQopType = topVintagesOurQopTypeRepository.findByType(checkType(type)).orElseThrow(
            () -> new ResourceNotFoundException("Top vintages by our qop for type: " + type + "\" not found.")
        );

        topVintagesOurQopTypeRepository.delete(topVintagesOurQopType);
    }


    // Elimina tutti i documenti dalla top vintages per qualità/prezzo (calcolati secondo la formula "base") per tipologia di vino.
    @Transactional
    public void deleteTopVintagesByQopPerType() {
        topVintagesQopTypeRepository.deleteAll();
    }

    // Elimina il documento di un certo tipo di vino dalla top vintages per qualità/prezzo (calcolati secondo la formula "base") per tipologia di vino.
    public void deleteTopVintagesByQopPerTypeByType(String type) {
        TopVintagesQopType topVintagesQopType = topVintagesQopTypeRepository.findByType(checkType(type)).orElseThrow(
            () -> new ResourceNotFoundException("Top vintages by qop for type: " + type + "\" not found.")
        );

        topVintagesQopTypeRepository.delete(topVintagesQopType);
    }


    // Elimina tutti i documenti dalla top vintages per valutazione media per tipologia di vino.
    @Transactional
    public void deleteTopVintagesByRatingsPerType() {
        topVintagesRatingsTypeRepository.deleteAll();
    }

    // Elimina il documento di un certo tipo di vino dalla top vintages per valutazione media per tipologia di vino.
    public void deleteTopVintagesByRatingsPerTypeByType(String type) {
        TopVintagesRatingsType topVintagesRatingsType = topVintagesRatingsTypeRepository.findByType(checkType(type)).orElseThrow(
            () -> new ResourceNotFoundException("Top vintages by ratings for type: " + type + "\" not found.")
        );

        topVintagesRatingsTypeRepository.delete(topVintagesRatingsType);
    }


    // Elimina tutti i documenti dalla top wines per valutazione media per tipologia di vino.
    @Transactional
    public void deleteTopWinesByRatingsPerType() {
        topWinesRatingsTypeRepository.deleteAll();
    }

    // Elimina il documento di un certo tipo di vino dalla top wines per valutazione media per tipologia di vino.
    public void deleteTopWinesByRatingsPerTypeByType(String type) {
        TopWinesRatingsType topWinesRatingsType = topWinesRatingsTypeRepository.findByType(checkType(type)).orElseThrow(
            () -> new ResourceNotFoundException("Top wines by ratings for type: " + type + "\" not found.")
        );

        topWinesRatingsTypeRepository.delete(topWinesRatingsType);
    }


    // Elimina tutti i documenti dalla top wineries per valutazione media dei propri vini.
    @Transactional
    public void deleteTopWineriesByWinesRatings() {
        topWineriesRatingsRepository.deleteAll();
    }

    // Elimina il documento di una certa winery dalla top wineries per valutazione media dei propri vini.
    public void deleteTopWineriesByWinesRatingsByWineryUsername(String winery_username) {
        TopWineriesRatings topWineriesRatings = topWineriesRatingsRepository.findByWineryUsername(winery_username).orElseThrow(
            () -> new ResourceNotFoundException("Top wineries by wines ratings for winery: " + winery_username + "\" not found.")
        );

        topWineriesRatingsRepository.delete(topWineriesRatings);
    }
    
    //// END of crud operations ////
    ////////////////////////////////
}
