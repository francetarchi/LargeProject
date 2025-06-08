package com.wineadvisor.wineadvisor.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import com.wineadvisor.wineadvisor.exception.ResourceAlreadyExistsException;
import com.wineadvisor.wineadvisor.exception.ResourceNotFoundException;
import com.wineadvisor.wineadvisor.DTO.users.UpdateUserDTO;
import com.wineadvisor.wineadvisor.DTO.users.addFavoriteDTO;
import com.wineadvisor.wineadvisor.DTO.utils.PasswordDTO;
import com.wineadvisor.wineadvisor.DTO.users.CreateUserDTO;
import com.wineadvisor.wineadvisor.exception.BadRequestException;
import com.wineadvisor.wineadvisor.repository.AdminRepository;
import com.wineadvisor.wineadvisor.repository.ReviewRepository;
import com.wineadvisor.wineadvisor.repository.UserNeo4jRepository;
import com.wineadvisor.wineadvisor.repository.UserRepository;
import com.wineadvisor.wineadvisor.repository.WineRepository;
import com.wineadvisor.wineadvisor.repository.WineryRepository;
import com.wineadvisor.wineadvisor.model.reviews.Review;
import com.wineadvisor.wineadvisor.model.users.User;
import com.wineadvisor.wineadvisor.model.users.fields.WineFavorite;
import com.wineadvisor.wineadvisor.model.utils.ReviewEmbedded;
import com.wineadvisor.wineadvisor.model.wines.fields.Vintage;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class UserService {
    ////////////////////////////////
    /////////// VARIABLES //////////
    ////////////////////////////////
    private final UserRepository userRepository;
    private final WineryRepository wineryRepository;
    private final AdminRepository adminRepository;
    private final ReviewRepository reviewRepository;
    private final WineRepository wineRepository;
    private final UserNeo4jRepository userNeo4jRepository; // inietto il repository per il neo4j

    private final PasswordEncoder passwordEncoder = PasswordDTO.passwordEncoder();

    private final MongoTemplate mongoTemplate;

    /////////// COSTANTI ////////////
    private static final int PAGE_SIZE = 20;



    ////////////////////////////////
    /////// PRIVATE METHODS ////////
    ////////////////////////////////
    
    ////////////////////////////////
    ///// Checking operations //////
    
    // Controlla che la pagina ritornata dalla repo sia valida e che sia consistente rispetto alle opzioni di paginazione richieste dal client.
    private void checkReturnedPage(Page<User> page, String notFoundMessage) throws ResourceNotFoundException, BadRequestException {
        if (page.getTotalElements() == 0) {
            throw new ResourceNotFoundException(notFoundMessage);
        }
        if (page.getPageable().getPageNumber() >= page.getTotalPages()) {
            throw new BadRequestException("Page requested too high.");
        }
    }

    // Controlla che i parametri passati per la creazione di un utente siano validi
    private void checkAccountParams(String username, String email, PasswordDTO passwordDTO) throws ResourceAlreadyExistsException, BadRequestException {
        if (userRepository.findByLogin_Username(username).isPresent()) {
            throw new ResourceAlreadyExistsException("User with username \"" + username + "\" already exists.");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new ResourceAlreadyExistsException("User with email \"" + email + "\" already exists.");
        }
        if (wineryRepository.findByLogin_Username(username).isPresent()) {
            throw new ResourceAlreadyExistsException("Winery with username \"" + username + "\" already exists.");
        }
        if (wineryRepository.findByEmail(email).isPresent()) {
            throw new ResourceAlreadyExistsException("Winery with email \"" + email + "\" already exists.");
        }
        if (adminRepository.findByLogin_Username(username).isPresent()) {
            throw new ResourceAlreadyExistsException("Admin with username \"" + username + "\" already exists.");
        }
        if (adminRepository.findByEmail(email).isPresent()) {
            throw new ResourceAlreadyExistsException("Admin with email \"" + email + "\" already exists.");
        }
        if (!passwordDTO.passwordPatternVerifier()) {
            throw new BadRequestException("Password does not meet the minimum requirements: at least 8 characters, 1 digit, 1 lowercase, 1 uppercase, 1 special character among \"!@#$%^&*()\\-_=+.,:;");
        }
        if (!passwordDTO.getNewPass().equals(passwordDTO.getConfirmPass())) {
            throw new BadRequestException("Passwords do not match.");
        }
    }
    
    /// END of checking operations //
    /////////////////////////////////
    
    
    /////////////////////////////////
    //// Asynchronous operations ////
    
    // Per ogni utente, scorre l'array dei likes e l'array dei dislikes: per ogni reviewId, controlla che la review esista ancora (se NON esiste più, elimina il reviewId dall'array)
    @Transactional
    @Scheduled(cron = "0 0 1 * * ?")    // Ogni giorno all'una di notte
    protected void cleanLikesAndDislikes() {
        System.out.println("--- INFO: Cleaning likes and dislikes from users...");
        userRepository.findAll().forEach(
            user -> {
                Boolean isUserUpdated = false;
                for (Long reviewId : user.getLikes()) {
                    if (!reviewRepository.existsById(reviewId)) {
                        user.getLikes().remove(reviewId);
                        isUserUpdated = true;
                    }
                }
                for (Long reviewId : user.getDislikes()) {
                    if (!reviewRepository.existsById(reviewId)) {
                        user.getDislikes().remove(reviewId);
                        isUserUpdated = true;
                    }
                }
                if (isUserUpdated) {
                    userRepository.save(user);
                }
            }
        );
        System.out.println("--- INFO: Likes and dislikes cleaned successfully.\n\n");
    }

    /// END of async. operations ///
    ////////////////////////////////


    ////////////////////////////////
    //// Aggregation pipelines /////

    // Aggiorna una volta a settimana la lista degli wineTips per ogni utente
    // @Scheduled(cron = "00 17 15 * * ?")    // Scheduling for debugging purposes
    @Scheduled(cron = "0 0 3 * * MON")      // Ogni lunedì alle 3 di notte
    protected void updateWineTipsPerUser() {
        // Defining the name of the starting collection
        final String SOURCE_COLLECTION_NAME = "users";

        // Instantiating the aggregation pipeline
        System.out.println("--- INFO: Declaring aggregation pipeline stages for updating field \"wine_tips\" in collection \"users\".");
        List<Document> pipeline = Arrays.asList(
                //// Stage 1: Project initial fields
                new Document("$project",
                        new Document("_id", 0L)
                                .append("username", "$login.username")
                                .append("favorite_wines", "$wine_favorites.id")),

                //// Stage 2: Lookup for user's top rated wines
                new Document("$lookup",
                        new Document("from", "reviews")
                                .append("let",
                                        new Document("userUsername", "$username"))
                                .append("pipeline", Arrays.asList(new Document("$match",
                                        new Document("$expr",
                                                new Document("$eq",
                                                        Arrays.asList("$user_id.username", "$$userUsername")))),
                                        new Document("$sort",
                                                new Document("rating", -1L)
                                                        .append("created_at", -1L)),
                                        new Document("$limit", 3L),
                                        new Document("$project",
                                                new Document("_id", 0L)
                                                        .append("wine_id", "$wine_id.id"))))
                                .append("as", "top_rated_wines")),

                //// Stage 3: Add fields and process top rated wines
                new Document("$addFields",
                        new Document("top_rated_wines",
                                new Document("$map",
                                        new Document("input", "$top_rated_wines")
                                                .append("as", "review")
                                                .append("in", "$$review.wine_id")))),

                //// Stage 4: Add fields to unify favorite wines and top rated wines
                new Document("$addFields",
                        new Document("wines",
                                new Document("$setUnion", Arrays.asList(
                                        new Document("$ifNull", Arrays.asList("$favorite_wines", Arrays.asList())),
                                        new Document("$ifNull", Arrays.asList("$top_rated_wines", Arrays.asList())))))),
                
                //// Stage 5: Project only necessary fields
                new Document("$project",
                        new Document("favorite_wines", 0L)
                                .append("top_rated_wines", 0L)),

                //// Stage 6: Unwind wines to get individual wine IDs
                new Document("$unwind",
                        new Document("path", "$wines")),

                //// Stage 7: Lookup to get wine details
                new Document("$lookup",
                        new Document("from", "wines")
                                .append("localField", "wines")
                                .append("foreignField", "_id")
                                .append("as", "wine")),

                //// Stage 8: Unwind wine to get individual wine details
                new Document("$unwind",
                        new Document("path", "$wine")),

                //// Stage 9: Project only necessary fields
                new Document("$project",
                        new Document("username", "$username")
                                .append("wine_id", "$wines")
                                .append("wine_style", "$wine.style.name")),

                //// Stage 10: Group by username and wine style to get unique wine tips per style for each user
                new Document("$group",
                        new Document("_id",
                                new Document("username", "$username")
                                        .append("wine_style", "$wine_style"))
                                .append("wine_id",
                                        new Document("$first", "$wine_id"))),

                //// Stage 11: Project only necessary fields
                new Document("$project",
                        new Document("_id", 0L)
                                .append("username", "$_id.username")
                                .append("wine_id", "$wine_id")
                                .append("wine_style", "$_id.wine_style")),
                                
                //// Stage 12: Lookup to get style details
                new Document("$lookup",
                        new Document("from", "wines")
                                .append("let",
                                        new Document("current_style_name", "$wine_style"))
                                .append("pipeline", Arrays.asList(new Document("$match",
                                        new Document("$expr",
                                                new Document("$eq",
                                                        Arrays.asList("$style.name", "$$current_style_name")))),
                                        new Document("$sort",
                                                new Document("statistics.ratings_average", -1L)
                                                        .append("statistics.ratings_count", -1L)),
                                        new Document("$limit", 25L),
                                        new Document("$project",
                                                new Document("_id", 0L)
                                                        .append("wine_id", "$_id")
                                                        .append("wine_name", "$name")
                                                        .append("wine_type", "$type")
                                                        .append("wine_style", "$style.name")
                                                        .append("wine_rating", "$statistics.ratings_average")
                                                        .append("wine_ratings_count", "$statistics.ratings_count")
                                                        .append("wine_price_avg",
                                                                new Document("$avg", "$vintages.price"))
                                                        .append("wine_image",
                                                                new Document(
                                                                        "$arrayElemAt", Arrays.asList(
                                                                                new Document("$map",
                                                                                        new Document("input",
                                                                                                "$vintages")
                                                                                                .append("as", "v")
                                                                                                .append("in",
                                                                                                        "$$v.image")),
                                                                                0L)))
                                                        .append("wine_created_at", "$created_at"))))
                                .append("as", "tips_by_style")),

                //// Stage 13: Unwind tips_by_style to get individual wine tips
                new Document("$unwind",
                        new Document("path", "$tips_by_style")
                                .append("preserveNullAndEmptyArrays", false)),

                //// Stage 14: Group by username to collect all wine tips for each user
                new Document("$group",
                        new Document("_id", "$username")
                                .append("wine_tips",
                                        new Document("$push", "$tips_by_style"))),

                //// Stage 15: Project final structure for each user
                new Document("$project",
                        new Document("_id", 0L)
                                .append("login.username", "$_id")
                                .append("wine_tips",
                                        new Document("$slice", Arrays.asList("$wine_tips", 100L)))),

                //// Stage 16: Merge results back into the users collection
                new Document("$merge",
                        new Document("into", "users")
                                .append("on", "login.username")
                                .append("whenMatched", Arrays.asList(new Document("$set",
                                        new Document("wine_tips", "$$new.wine_tips"))))
                                .append("whenNotMatched", "discard")));


        // Ensuring existence of indexes needed for lookups
        mongoTemplate
            .indexOps("users")
            .ensureIndex(
                new Index()
                    .on("login.username", Sort.Direction.ASC)
                    .named("on_username_UNIQUE")
                    .unique()
                    );
        System.out.println("--- INFO: Ensured UNIQUE index on field \"username\" for collection \"" + SOURCE_COLLECTION_NAME + "\".");

        mongoTemplate
            .indexOps("reviews")
            .ensureIndex(
                new Index()
                    .on("user_id.username", Sort.Direction.ASC)
                    .on("rating", Sort.Direction.DESC)
                    .on("created_at", Sort.Direction.DESC)
                    .named("index_for_wine_tips_and_new_wine_tips")
            );
        System.out.println("--- INFO: Ensured index on fields \"user_id.username\", \"rating\" and \"created_at\" for collection \"reviews\".");

        mongoTemplate
            .indexOps("wines")
            .ensureIndex(
                new Index()
                    .on("style.name", Sort.Direction.ASC)
                    .on("statistics.ratings_average", Sort.Direction.DESC)
                    .on("statistics.ratings_count", Sort.Direction.DESC)
                    .named("index_for_wine_tips")
            );
        System.out.println("--- INFO: Ensured index on fields \"style.name\", \"statistics.ratings_average\" and \"statistics.ratings_count\" for collection \"wines\".");

        
        // Executing the pipeline
        System.out.println("--- INFO: Executing aggregation pipeline...");
        mongoTemplate.getCollection(SOURCE_COLLECTION_NAME).aggregate(pipeline).toCollection();

        System.out.println("--- INFO: Field \"wine_tips\" in collection \"users\" updated successfully.\n\n");
    }


    // Aggiorna una volta a settimana la lista dei newWineTips per ogni utente
    // @Scheduled(cron = "00 17 15 * * ?")    // Scheduling for debugging purposes
    @Scheduled(cron = "0 0 3 * * MON")     // Ogni lunedì alle 3 di notte
    protected void updateNewWineTipsPerUser() {
        // Defining the name of the starting collection
        final String SOURCE_COLLECTION_NAME = "users";

        // Instantiating the aggregation pipeline
        System.out.println("--- INFO: Declaring aggregation pipeline stages for updating field \"new_wine_tips\" in collection \"users\".");
        List<Document> pipeline = Arrays.asList(
                //// Stage 1: Project initial fields
                new Document("$project",
                        new Document("_id", 0L)
                                .append("username", "$login.username")
                                .append("favorite_wines", "$wine_favorites.id")),

                //// Stage 2: Lookup for user's favorite wines
                new Document("$lookup",
                        new Document("from", "reviews")
                                .append("let",
                                        new Document("userUsername", "$username"))
                                .append("pipeline", Arrays.asList(new Document("$match",
                                        new Document("$expr",
                                                new Document("$eq",
                                                        Arrays.asList("$user_id.username", "$$userUsername")))),
                                        new Document("$sort",
                                                new Document("rating", -1L)
                                                        .append("created_at", -1L)),
                                        new Document("$limit", 3L),
                                        new Document("$project",
                                                new Document("_id", 0L)
                                                        .append("wine_id", "$wine_id.id"))))
                                .append("as", "top_rated_wines")),

                //// Stage 3: Add fields and process top rated wines
                new Document("$addFields",
                        new Document("top_rated_wines",
                                new Document("$map",
                                        new Document("input", "$top_rated_wines")
                                                .append("as", "review")
                                                .append("in", "$$review.wine_id")))),

                //// Stage 4: Add fields to unify favorite wines and top rated wines
                new Document("$addFields",
                        new Document("wines",
                                new Document("$setUnion", Arrays.asList(
                                        new Document("$ifNull", Arrays.asList("$favorite_wines", Arrays.asList())),
                                        new Document("$ifNull", Arrays.asList("$top_rated_wines", Arrays.asList())))))),

                //// Stage 5: Project only necessary fields
                new Document("$project",
                        new Document("favorite_wines", 0L)
                                .append("top_rated_wines", 0L)),

                //// Stage 6: Unwind wines to get individual wine IDs
                new Document("$unwind",
                        new Document("path", "$wines")),

                //// Stage 7: Lookup to get wine details
                new Document("$lookup",
                        new Document("from", "wines")
                                .append("localField", "wines")
                                .append("foreignField", "_id")
                                .append("as", "wine")),

                //// Stage 8: Unwind wine to get individual wine details
                new Document("$unwind",
                        new Document("path", "$wine")),

                //// Stage 9: Project only necessary fields
                new Document("$project",
                        new Document("username", "$username")
                                .append("wine_id", "$wines")
                                .append("wine_style", "$wine.style.name")),

                //// Stage 10: Group by username and wine style to get unique wine tips per style for each user
                new Document("$group",
                        new Document("_id",
                                new Document("username", "$username")
                                        .append("wine_style", "$wine_style"))
                                .append("wine_id",
                                        new Document("$first", "$wine_id"))),

                //// Stage 11: Project only necessary fields
                new Document("$project",
                        new Document("_id", 0L)
                                .append("username", "$_id.username")
                                .append("wine_id", "$wine_id")
                                .append("wine_style", "$_id.wine_style")),

                //// Stage 12: Lookup to get style details of wines created in the last 6 months
                new Document("$lookup",
                        new Document("from", "wines")
                                .append("let",
                                        new Document("current_style_name", "$wine_style"))
                                .append("pipeline", Arrays.asList(new Document("$match",
                                        new Document("$expr",
                                                new Document("$and",
                                                        Arrays.asList(
                                                                new Document("$eq",
                                                                        Arrays.asList("$style.name",
                                                                                "$$current_style_name")),
                                                                new Document("$gte", Arrays.asList("$created_at",
                                                                        new Document("$dateSubtract",
                                                                                new Document("startDate", "$$NOW")
                                                                                        .append("unit", "month")
                                                                                        .append("amount", 6L)))))))),
                                        new Document("$sort",
                                                new Document("created_at", -1L)),
                                        new Document("$limit", 25L),
                                        new Document("$project",
                                                new Document("_id", 0L)
                                                        .append("wine_id", "$_id")
                                                        .append("wine_name", "$name")
                                                        .append("wine_type", "$type")
                                                        .append("wine_style", "$style.name")
                                                        .append("wine_rating", "$statistics.ratings_average")
                                                        .append("wine_ratings_count", "$statistics.ratings_count")
                                                        .append("wine_price_avg",
                                                                new Document("$avg", "$vintages.price"))
                                                        .append("wine_image",
                                                                new Document(
                                                                        "$arrayElemAt", Arrays.asList(
                                                                                new Document("$map",
                                                                                        new Document("input",
                                                                                                "$vintages")
                                                                                                .append("as", "v")
                                                                                                .append("in",
                                                                                                        "$$v.image")),
                                                                                0L)))
                                                        .append("wine_created_at", "$created_at"))))
                                .append("as", "tips_by_style")),

                //// Stage 13: Unwind tips_by_style to get individual wine tips
                new Document("$unwind",
                        new Document("path", "$tips_by_style")
                                .append("preserveNullAndEmptyArrays", false)),

                //// Stage 14: Group by username to collect all new wine tips for each user
                new Document("$group",
                        new Document("_id", "$username")
                                .append("new_wine_tips",
                                        new Document("$push", "$tips_by_style"))),

                //// Stage 15: Project final structure for each user
                new Document("$project",
                        new Document("_id", 0L)
                                .append("login.username", "$_id")
                                .append("new_wine_tips",
                                        new Document("$slice", Arrays.asList("$new_wine_tips", 100L)))),

                //// Stage 16: Merge results back into the users collection
                new Document("$merge",
                        new Document("into", "users")
                                .append("on", "login.username")
                                .append("whenMatched", Arrays.asList(new Document("$set",
                                        new Document("new_wine_tips", "$$new.new_wine_tips"))))
                                .append("whenNotMatched", "discard")));


        // Ensuring existence of indexes needed for lookups
        mongoTemplate
            .indexOps("users")
            .ensureIndex(
                new Index()
                    .on("login.username", Sort.Direction.ASC)
                    .named("on_username_UNIQUE")
                    .unique()
                    );
        System.out.println("--- INFO: Ensured UNIQUE index on field \"username\" for collection \"" + SOURCE_COLLECTION_NAME + "\".");

        mongoTemplate
            .indexOps("reviews")
            .ensureIndex(
                new Index()
                    .on("user_id.username", Sort.Direction.ASC)
                    .on("rating", Sort.Direction.DESC)
                    .on("created_at", Sort.Direction.DESC)
                    .named("index_for_wine_tips_and_new_wine_tips")
            );
        System.out.println("--- INFO: Ensured index on fields \"user_id.username\", \"rating\" and \"created_at\" for collection \"reviews\".");

        mongoTemplate
            .indexOps("wines")
            .ensureIndex(
                new Index()
                    .on("style.name", Sort.Direction.ASC)
                    .on("created_at", Sort.Direction.DESC)
                    .named("index_for_new_wine_tips")
            );
        System.out.println("--- INFO: Ensured index on fields \"style.name\", \"statistics.ratings_average\" and \"statistics.ratings_count\" for collection \"wines\".");


        // Executing the pipeline
        System.out.println("--- INFO: Executing aggregation pipeline...");
        mongoTemplate.getCollection(SOURCE_COLLECTION_NAME).aggregate(pipeline).toCollection();

        System.out.println("--- INFO: Field \"new_wine_tips\" in collection \"users\" updated successfully.\n\n");
    }

    //// END of aggr. pipelines ////
    ////////////////////////////////
    

    ////////////////////////////////
    ///// Operations on users //////

    // Ricerca una review nella collection users (per ogni user ho le reviews) e ne aggiorna correttamente il numero di likes e dislikes
    private void updateUser_Reviews_LikesCountAndDislikesCountByReviewId(Long targetReviewId, Integer targetYear, Long updatedLikesCount, Long updatedDislikesCount) {
        userRepository
            .findByReviews_ReviewId(targetReviewId)
            .map(
                targetUser -> {
                    for (ReviewEmbedded r : targetUser.getReviews()) {
                        if (r.getReviewId().equals(targetReviewId)) {
                            r.setLikesCount(updatedLikesCount);
                            r.setDislikesCount(updatedDislikesCount);
                            break;
                        }
                    }
                    
                    return userRepository.save(targetUser);
                }
            );
    }

    /// END of operat. on users ///
    ///////////////////////////////
    
    
    ///////////////////////////////
    ///// Operations on wines /////
    
    // Ricerca una review nella collection wines (per ogni wine, per ogni vintage ho le reviews) e ne aggiorna correttamente la thumbnail dell'utente
    private void updateWine_Vintages_Reviews_UserId_ThumbnailByReviewId(Long targetReviewId, Integer targetYear, String updatedThumbnail) {
        wineRepository
            .findByVintages_Reviews_ReviewId(targetReviewId)
            .map(
                targetWine -> {
                    for (Vintage v : targetWine.getVintages()) {
                        if (v.getYear().equals(targetYear)) {
                            for (ReviewEmbedded r : v.getReviews()) {
                                if (r.getReviewId().equals(targetReviewId)) {
                                    r.getUserId().setThumbnail(updatedThumbnail);
                                    break;
                                }
                            }
                            break;
                        }
                    }
                    
                    return wineRepository.save(targetWine);
                }
            );
    }

    // Ricerca una review nella collection wines (per ogni wine, per ogni vintage ho le reviews) e ne aggiorna correttamente lo username dell'utente
    private void updateWine_Vintages_Reviews_UserId_UsernameByReviewId(Long targetReviewId, Integer targetYear, String updatedUsername) {
        wineRepository
            .findByVintages_Reviews_ReviewId(targetReviewId)
            .map(
                targetWine -> {
                    for (Vintage v : targetWine.getVintages()) {
                        if (v.getYear().equals(targetYear)) {
                            for (ReviewEmbedded r : v.getReviews()) {
                                if (r.getReviewId().equals(targetReviewId)) {
                                    r.getUserId().setUsername(updatedUsername);
                                    break;
                                }
                            }
                            break;
                        }
                    }
                    
                    return wineRepository.save(targetWine);
                }
            );
    }

    // Ricerca una review nella collection wines (per ogni wine, per ogni vintage ho le reviews) e la elimina
    private void deleteWine_Vintages_ReviewByReviewId(Long targetReviewId, Integer targetYear) {
        wineRepository
            .findByVintages_Reviews_ReviewId(targetReviewId)
            .map(
                targetWine -> {
                    for (Vintage v : targetWine.getVintages()) {
                        if (v.getYear().equals(targetYear)) {
                            for (ReviewEmbedded r : v.getReviews()) {
                                if (r.getReviewId().equals(targetReviewId)) {
                                    v.getReviews().remove(r);
                                    break;
                                }
                            }
                            break;
                        }
                    }
                    
                    return wineRepository.save(targetWine);
                }
            );
    }

    // Ricerca una review nella collection wines (per ogni wine, per ogni vintage ho le reviews) e ne aggiorna correttamente il numero di likes e dislikes
    private void updateWine_Vintages_Reviews_LikesCountAndDislikesCountByReviewId(Long targetReviewId, Integer targetYear, Long updatedLikesCount, Long updatedDislikesCount) {
        wineRepository
            .findByVintages_Reviews_ReviewId(targetReviewId)
            .map(
                targetWine -> {
                    for (Vintage v : targetWine.getVintages()) {
                        if (v.getYear().equals(targetYear)) {
                            for (ReviewEmbedded r : v.getReviews()) {
                                if (r.getReviewId().equals(targetReviewId)) {
                                    r.setLikesCount(updatedLikesCount);
                                    r.setDislikesCount(updatedDislikesCount);
                                    break;
                                }
                            }
                            break;
                        }
                    }
                    
                    return wineRepository.save(targetWine);
                }
            );
    }
    
    //// END of operat. on wines ////
    /////////////////////////////////


    /////////////////////////////////
    ///// Operations on reviews /////
    
    // Ricerca le review scritte da un certo utente nella collection reviews e ne aggiorna correttamente la thumbnail dell'utente
    private void updateReview_UserId_ThumbnailByUserUsername(String targetUsername, String updatedThumbnail) {
        reviewRepository
            .findByUserId_Username(targetUsername)
            .forEach(
                review -> {
                    review.getUserId().setThumbnail(updatedThumbnail);
                    reviewRepository.save(review);

                    // Aggiorno la review nella collection "wines" (qualora sia presente)
                    updateWine_Vintages_Reviews_UserId_ThumbnailByReviewId(review.getId(), review.getWineId().getYear(), updatedThumbnail);
                }
            );
    }

    // Ricerca le review scritte da un certo utente nella collection reviews e ne aggiorna correttamente lo username dell'utente
    private void updateReview_UserId_UsernameByUserUsername(String targetUsername, String updatedUsername) {
        reviewRepository
            .findByUserId_Username(targetUsername)
            .forEach(
                review -> {
                    review.getUserId().setUsername(updatedUsername);
                    reviewRepository.save(review);

                    // Aggiorno la review nella collection "wines" (qualora sia presente)
                    updateWine_Vintages_Reviews_UserId_UsernameByReviewId(review.getId(), review.getWineId().getYear(), updatedUsername);
                }
            );
    }

    // Ricerca le review scritte da un certo utente nella collection reviews e le elimina
    private void deleteReviewByUserUsername(String targetUsername) {
        reviewRepository
            .findByUserId_Username(targetUsername)
            .forEach(
                review -> {
                    reviewRepository.delete(review);

                    // Elimino la review dalla collection "wines" (qualora sia presente)
                    deleteWine_Vintages_ReviewByReviewId(review.getId(), review.getWineId().getYear());
                }
            );
    }

    /// END of operat. on reviews ///
    /////////////////////////////////


    
    /////////////////////////////////
    //////// PUBLIC METHODS /////////
    /////////////////////////////////

    /////////////////////////////////
    /////// CRUD operations /////////

    /// CREATE operations ///
    // Aggiunge un utente alla collection "users" del database
    @Transactional
    public User createUser(CreateUserDTO createUserDTO) throws ResourceAlreadyExistsException, BadRequestException {
        checkAccountParams(createUserDTO.getUsername(), createUserDTO.getEmail(), createUserDTO.getPasswordDTO());
        
        User newUser = createUserDTO.toUser();
        newUser.adjustFieldsForCreation(passwordEncoder.encode(createUserDTO.getPasswordDTO().getNewPass()));
        
        User savedUser = userRepository.save(newUser);

        // Sincronizzazione con Neo4j
        userNeo4jRepository.createUser(savedUser.getLogin().getUsername(), savedUser.getName().getFirst(), savedUser.getName().getLast(), savedUser.getPicture().getThumbnail());
        
        return savedUser;
    }
    
    
    /// READ operations ///
    // Restituisce tutti gli utenti presenti nella collection "users" del database
    public Page<User> getAllUsers(Integer page) throws ResourceNotFoundException, BadRequestException {
        Page<User> users = userRepository.findAll(PageRequest.of(page, PAGE_SIZE));
        checkReturnedPage(users, "No users found.");
        return users;
    }

    // Restituisce tutti gli utenti con un determinato nome e cognome
    public Page<User> getUsersByFullName(String firstName, String lastName, Integer page) throws ResourceNotFoundException, BadRequestException {
        Page<User> users = userRepository.findByName_FirstContainingIgnoreCaseAndName_LastContainingIgnoreCase(firstName, lastName, PageRequest.of(page, PAGE_SIZE));
        checkReturnedPage(users, "Users with first name \"" + firstName + "\" and last name \"" + lastName + "\" not found.");
        return users;
    }

    // Restituisce tutti gli utenti con un determinato nome
    public Page<User> getUsersByFirstName(String firstName, Integer page) throws ResourceNotFoundException, BadRequestException {
        Page<User> users = userRepository.findByName_FirstContainingIgnoreCase(firstName, PageRequest.of(page, PAGE_SIZE));
        checkReturnedPage(users, "Users with first name \"" + firstName + "\" not found.");
        return users;
    }

    // Restituisce tutti gli utenti con un determinato cognome
    public Page<User> getUsersByLastName(String lastName, Integer page) throws ResourceNotFoundException, BadRequestException {
        Page<User> users = userRepository.findByName_LastContainingIgnoreCase(lastName, PageRequest.of(page, PAGE_SIZE));
        checkReturnedPage(users, "Users with last name \"" + lastName + "\" not found.");
        return users;
    }

    // Restituisce un utente con un determinato username
    public User getUserByUsername(String username) throws ResourceNotFoundException {
        User user = userRepository.findByLogin_Username(username).orElseThrow(
            () -> new ResourceNotFoundException("User with username \"" + username + "\" not found.")
        );
        return user;
    }

    public Map<String, Object> getUserFromGraph(String username) {
        return userNeo4jRepository.findUserByUsername(username);
    }


    /// UPDATE operations ///
    // Cerca il documento di un utente con un determinato username e aggiorna l'intero documento con il nuovo passato come argomento
    @Transactional
    public User updateUser(String targetUsername, UpdateUserDTO updateUserDTO) throws ResourceNotFoundException, ResourceAlreadyExistsException {
        return userRepository
            .findByLogin_Username(targetUsername)
            .map(
                targetUser -> {
                    Boolean isThumbnailChanged = targetUser.getPicture().getThumbnail().equals(updateUserDTO.getPictureDTO().getThumbnail()) ? false : true;
                    User userWithSameEmail = userRepository.findByEmail(updateUserDTO.getEmail()).orElse(null);
                    if ( userWithSameEmail != null && !userWithSameEmail.getLogin().getUsername().equals(targetUser.getLogin().getUsername()) ) {
                        throw new ResourceAlreadyExistsException("User with username \"" + targetUser.getLogin().getUsername() + "\" not updatable because email \"" + updateUserDTO.getEmail() + "\" is already used by another user.");
                    }

                    targetUser = updateUserDTO.toUser(targetUser);
                    if (isThumbnailChanged && targetUser.getReviews().size() > 0) {
                        for (ReviewEmbedded review : targetUser.getReviews()) {
                            review.getUserId().setThumbnail(updateUserDTO.getPictureDTO().getThumbnail());
                        }

                        // Aggiorno tutte le review dell'utente nella collection "reviews"
                        updateReview_UserId_ThumbnailByUserUsername(targetUser.getLogin().getUsername(), targetUser.getPicture().getThumbnail());
                    }

                    // Finalizzo gli aggiornamenti in modo da evitare incosistenze nel database
                    targetUser.adjustFieldsForUpdate();

                    User savedUser = userRepository.save(targetUser);

                    // Sincronizzazione con Neo4j
                    userNeo4jRepository.updateUser(savedUser.getLogin().getUsername(), savedUser.getName().getFirst(), savedUser.getName().getLast(), savedUser.getPicture().getThumbnail());

                    return savedUser;
                }
            )
            .orElseThrow(
                () -> new ResourceNotFoundException("User with username \"" + targetUsername + "\" not updatable because it does not exist.")
            );
    }

    // Cerca il documento di un utente e ne modifica lo username
    @Transactional
    public User updateUserUsername(String targetUsername, String newUsername) throws ResourceNotFoundException, ResourceAlreadyExistsException, BadRequestException{
        if (targetUsername.equals(newUsername)) {
            throw new BadRequestException("Username not updatable because it is the same as the old one.");
        }
        if (userRepository.findByLogin_Username(newUsername).isPresent()) {
            throw new ResourceAlreadyExistsException("Username not updatable because \"" + newUsername + "\" is already used by another user.");
        }

        return userRepository
            .findByLogin_Username(targetUsername)
            .map(
                targetUser -> {
                    targetUser.getLogin().setUsername(newUsername.trim());
                    if (targetUser.getReviews().size() > 0) {
                        for (ReviewEmbedded review : targetUser.getReviews()) {
                            review.getUserId().setUsername(newUsername.trim());
                        }

                        // Aggiorno tutte le review dell'utente nella collection "reviews"
                        updateReview_UserId_UsernameByUserUsername(targetUsername, newUsername.trim());
                    }

                    User savedUser = userRepository.save(targetUser);

                    // Sincronizzazione con Neo4j
                    userNeo4jRepository.updateUserUsername(targetUsername, newUsername.trim());

                    return savedUser;
                }
            )
            .orElseThrow(
                () -> new ResourceNotFoundException("Username \"" + targetUsername + "\" not updatable because no user uses it.")
            );
    }

    // Cerca il documento di un utente e ne modifica la password
    public User updateUserPassword(String targetUsername, PasswordDTO passwordDTO) throws IllegalArgumentException, ResourceNotFoundException {
        passwordDTO.setOldPass(passwordDTO.getOldPass().trim());
        passwordDTO.setNewPass(passwordDTO.getNewPass().trim());
        passwordDTO.setConfirmPass(passwordDTO.getConfirmPass().trim());

        return userRepository
            .findByLogin_Username(targetUsername)
            .map(
                targetUser -> {
                    if (!passwordEncoder.matches(passwordDTO.getOldPass(), targetUser.getLogin().getPassword())) {
                        throw new IllegalArgumentException("Password of user with username \"" + targetUsername + "\" not updatable because old password is wrong.");
                    }
                    if (passwordDTO.getNewPass().equals(passwordDTO.getOldPass())) {
                        throw new IllegalArgumentException("Password of user with username \"" + targetUsername + "\" not updatable because new password is the same as the old one.");
                    }
                    if (!passwordDTO.getNewPass().equals(passwordDTO.getConfirmPass())) {
                        throw new IllegalArgumentException("Password of user with username \"" + targetUsername + "\" not updatable because new passwords do not match.");
                    }
                    if (!passwordDTO.passwordPatternVerifier()) {
                        throw new IllegalArgumentException("Password does not meet the minimum requirements: at least 8 characters, 1 digit, 1 lowercase, 1 uppercase, 1 special character among \"!@#$%^&*()\\-_=+.,:;");
                    }

                    targetUser.getLogin().setPassword(passwordEncoder.encode(passwordDTO.getNewPass()));
                    
                    return userRepository.save(targetUser);
                }
            )
            .orElseThrow(
                () -> new ResourceNotFoundException("User with username \"" + targetUsername + "\" not updatable because it does not exist.")
            );
    }

    // Cerca il documento di un utente e aggiunge un reviewId alla sua lista di likes
    @Transactional
    public User addLike(String username, Long reviewId) throws IllegalArgumentException, ResourceNotFoundException, ResourceAlreadyExistsException {
        return userRepository
            .findByLogin_Username(username)
            .map(user -> {
                // Prendo il riferimento alla review interessata
                Review review = reviewRepository.findById(reviewId).orElse(null);
                if (review == null) {
                    throw new ResourceNotFoundException("Review with id " + reviewId + " not found.");
                }

                // Controllo che l'utente non sia lo stesso che ha scritto la recensione
                if (username.equals(review.getUserId().getUsername())) {
                    throw new IllegalArgumentException("A user cannot like his own reviews.");
                }

                // Controllo che l'utente non abbia già messo like alla recensione
                if (user.getLikes().contains(reviewId)) {
                    throw new ResourceAlreadyExistsException("User with username " + username + " has already liked this review.");
                }

                // Se l'utente aveva messo in precedenza dislike alla recensione, rimuovo il dislike e decremento il numero di dislikes della recensione
                if (user.getDislikes().contains(reviewId)) {
                    user.getDislikes().remove(reviewId);
                    review.setDislikesCount(review.getDislikesCount() - 1);
                }

                // Aggiungo l'utente alla lista di chi ha messo like alla recensione e incremento il numero di likes della recensione
                user.getLikes().add(reviewId);
                review.setLikesCount(review.getLikesCount() + 1);

                // Aggiorno la review nella collection "wines" (qualora sia presente)
                updateWine_Vintages_Reviews_LikesCountAndDislikesCountByReviewId(reviewId, review.getWineId().getYear(), review.getLikesCount(), review.getDislikesCount());

                // Aggiorno la review nella collection "users" (qualora sia presente)
                updateUser_Reviews_LikesCountAndDislikesCountByReviewId(reviewId, review.getWineId().getYear(), review.getLikesCount(), review.getDislikesCount());

                return userRepository.save(user);
            })
            .orElseThrow(
                () -> new ResourceNotFoundException("User with username \"" + username + "\" not found.")
            );
    }

    // Cerca il documento di un utente e rimuove un reviewId dalla sua lista di likes
    @Transactional
    public User removeLike(String username, Long reviewId) throws IllegalArgumentException, ResourceNotFoundException, ResourceAlreadyExistsException {
        return userRepository
            .findByLogin_Username(username)
            .map(user -> {
                // Prendo il riferimento alla review interessata
                Review review = reviewRepository.findById(reviewId).orElse(null);
                if (review == null) {
                    throw new ResourceNotFoundException("Review with id " + reviewId + " not found.");
                }

                // Controllo che l'utente non sia lo stesso che ha scritto la recensione
                if (username.equals(review.getUserId().getUsername())) {
                    throw new IllegalArgumentException("A user cannot like his own reviews.");
                }

                // Controllo che l'utente abbia messo like alla recensione
                if (!user.getLikes().contains(reviewId)) {
                    throw new ResourceAlreadyExistsException("User with username " + username + " has not liked this review.");
                }

                // Rimuovo l'utente dalla lista di chi ha messo like alla recensione e
                // decremento il numero di likes della recensione
                user.getLikes().remove(reviewId);
                review.setLikesCount(review.getLikesCount() - 1);

                // Aggiorno la review nella collection "wines" (qualora sia presente)
                updateWine_Vintages_Reviews_LikesCountAndDislikesCountByReviewId(reviewId,
                        review.getWineId().getYear(), review.getLikesCount(), review.getDislikesCount());

                // Aggiorno la review nella collection "users" (qualora sia presente)
                updateUser_Reviews_LikesCountAndDislikesCountByReviewId(reviewId, review.getWineId().getYear(),
                        review.getLikesCount(), review.getDislikesCount());

                return userRepository.save(user);
            })
            .orElseThrow(
                    () -> new ResourceNotFoundException("User with username \"" + username + "\" not found.")
            );
    }

    // Cerca il documento di un utente e aggiunge un reviewId alla sua lista di dislikes
    @Transactional
    public User addDislike(String username, Long reviewId) throws IllegalArgumentException, ResourceNotFoundException, ResourceAlreadyExistsException {
        return userRepository
            .findByLogin_Username(username)
            .map(user -> {
                // Prendo il riferimento alla review interessata
                Review review = reviewRepository.findById(reviewId).orElse(null);
                if (review == null) {
                    throw new ResourceNotFoundException("Review with id " + reviewId + " not found.");
                }

                // Controllo che l'utente non sia lo stesso che ha scritto la recensione
                if (username.equals(review.getUserId().getUsername())) {
                    throw new IllegalArgumentException("A user cannot dislike his own reviews.");
                }

                // Controllo che l'utente non abbia già messo dislike alla recensione
                if (user.getDislikes().contains(reviewId)) {
                    throw new ResourceAlreadyExistsException("User with username " + username + " has already disliked this review.");
                }

                // Se l'utente aveva messo in precedenza like alla recensione, rimuovo il like e
                // decremento il numero di likes della recensione
                if (user.getLikes().contains(reviewId)) {
                    user.getLikes().remove(reviewId);
                    review.setDislikesCount(review.getLikesCount() - 1);
                }

                // Aggiungo l'utente alla lista di chi ha messo dislike alla recensione e
                // incremento il numero di dislikes della recensione
                user.getDislikes().add(reviewId);
                review.setLikesCount(review.getDislikesCount() + 1);

                // Aggiorno la review nella collection "wines" (qualora sia presente)
                updateWine_Vintages_Reviews_LikesCountAndDislikesCountByReviewId(reviewId,
                        review.getWineId().getYear(), review.getLikesCount(), review.getDislikesCount());

                // Aggiorno la review nella collection "users" (qualora sia presente)
                updateUser_Reviews_LikesCountAndDislikesCountByReviewId(reviewId, review.getWineId().getYear(),
                        review.getLikesCount(), review.getDislikesCount());

                return userRepository.save(user);
            })
            .orElseThrow(
                    () -> new ResourceNotFoundException("User with username \"" + username + "\" not found.")
            );
    }

    // Cerca il documento di un utente e rimuove un reviewId dalla sua lista di dislikes
    @Transactional
    public User removeDislike(String username, Long reviewId) throws IllegalArgumentException, ResourceNotFoundException, ResourceAlreadyExistsException {
        return userRepository
            .findByLogin_Username(username)
            .map(user -> {
                // Prendo il riferimento alla review interessata
                Review review = reviewRepository.findById(reviewId).orElse(null);
                if (review == null) {
                    throw new ResourceNotFoundException("Review with id " + reviewId + " not found.");
                }

                // Controllo che l'utente non sia lo stesso che ha scritto la recensione
                if (username.equals(review.getUserId().getUsername())) {
                    throw new IllegalArgumentException("A user cannot dislike his own reviews.");
                }

                // Controllo che l'utente abbia messo dislike alla recensione
                if (!user.getDislikes().contains(reviewId)) {
                    throw new ResourceAlreadyExistsException("User with username " + username + " has not disliked this review.");
                }

                // Rimuovo l'utente dalla lista di chi ha messo dislike alla recensione e
                // decremento il numero di dislikes della recensione
                user.getDislikes().remove(reviewId);
                review.setLikesCount(review.getDislikesCount() - 1);

                // Aggiorno la review nella collection "wines" (qualora sia presente)
                updateWine_Vintages_Reviews_LikesCountAndDislikesCountByReviewId(reviewId,
                        review.getWineId().getYear(), review.getLikesCount(), review.getDislikesCount());

                // Aggiorno la review nella collection "users" (qualora sia presente)
                updateUser_Reviews_LikesCountAndDislikesCountByReviewId(reviewId, review.getWineId().getYear(),
                        review.getLikesCount(), review.getDislikesCount());

                return userRepository.save(user);
            })
            .orElseThrow(
                    () -> new ResourceNotFoundException("User with username \"" + username + "\" not found.")
            );
    }

    // Cerca il documento di un utente e aggiunge un vino alla sua lista di preferiti
    public User addFavorite(String username, addFavoriteDTO addFavoriteDTO) throws ResourceNotFoundException, ResourceAlreadyExistsException {
        return userRepository
            .findByLogin_Username(username)
            .map(user -> {
                // Controllo che l'utente non abbia già messo il vino tra i preferiti
                for (WineFavorite w : user.getWineFavorites()) {
                    if (w.getId().equals(addFavoriteDTO.getWineId())) {
                        throw new ResourceAlreadyExistsException("User with username " + username + " has already added this wine to favorites.");
                    }
                }

                // Aggiungo il vino alla lista dei preferiti dell'utente
                user.getWineFavorites().add(addFavoriteDTO.toWineFavorite());

                return userRepository.save(user);
            })
            .orElseThrow(
                    () -> new ResourceNotFoundException("User with username \"" + username + "\" not found.")
            );
    }

    // Cerca il documento di un utente e rimuove un vino dalla sua lista di preferiti
    public User removeFavorite(String username, Long wineId) throws ResourceNotFoundException, ResourceAlreadyExistsException {
        return userRepository
            .findByLogin_Username(username)
            .map(user -> {
                // Controllo che l'utente abbia messo il vino tra i preferiti
                for (WineFavorite w : user.getWineFavorites()) {
                    if (w.getId().equals(wineId)) {
                        // Rimuovo il vino dalla lista dei preferiti dell'utente
                        user.getWineFavorites().remove(w);
                        return userRepository.save(user);
                    }
                }

                throw new ResourceAlreadyExistsException("User with username " + username + " has not added this wine to favorites.");
            })
            .orElseThrow(
                    () -> new ResourceNotFoundException("User with username \"" + username + "\" not found.")
            );
    }


    /// DELETE operations ///
    // Elimina tutti gli utenti presenti nella collection "users" del database
    @Transactional
    public void deleteAllUsers() {
        userRepository.deleteAll();

        // Sincronizzazione con Neo4j
        userNeo4jRepository.deleteAllUsers();
    }

    // Elimina un utente con un determinato username
    @Transactional
    public void deleteUser(String targetUsername) throws ResourceNotFoundException {
        final User targetUser = userRepository
            .findByLogin_Username(targetUsername)
            .orElseThrow(
                () -> new ResourceNotFoundException("User with username \"" + targetUsername + "\" not deletable because user does not exists.")
            );

        // Controllo se l'utente ha scritto almeno una recensione
        if (targetUser.getReviews().size() > 0) {
            // Elimino tutte le review dell'utente nella collection "reviews"
            deleteReviewByUserUsername(targetUsername);
        }

        userRepository.delete(targetUser);

        // Sincronizzazione con Neo4j
        userNeo4jRepository.deleteUserByUsername(targetUsername);
    }

    //// END of crud operations ////
    ////////////////////////////////
}
