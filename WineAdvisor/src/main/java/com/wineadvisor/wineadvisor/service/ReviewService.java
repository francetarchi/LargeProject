package com.wineadvisor.wineadvisor.service;

import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.time.Instant;

import com.wineadvisor.wineadvisor.repository.ReviewNeo4jRepository;
import com.wineadvisor.wineadvisor.repository.ReviewRepository;
import com.wineadvisor.wineadvisor.repository.UserRepository;
import com.wineadvisor.wineadvisor.repository.WineRepository;
import com.wineadvisor.wineadvisor.model.reviews.Review;
import com.wineadvisor.wineadvisor.model.reviews.fields.UserId;
import com.wineadvisor.wineadvisor.model.reviews.fields.WineId;
import com.wineadvisor.wineadvisor.model.users.User;
import com.wineadvisor.wineadvisor.model.utils.ReviewEmbedded;
import com.wineadvisor.wineadvisor.model.wines.*;
import com.wineadvisor.wineadvisor.model.wines.fields.Vintage;
import com.wineadvisor.wineadvisor.DTO.reviews.CreateReviewDTO;
import com.wineadvisor.wineadvisor.DTO.reviews.UpdateReviewDTO;
import com.wineadvisor.wineadvisor.exception.AccessDeniedException;
import com.wineadvisor.wineadvisor.exception.BadRequestException;
import com.wineadvisor.wineadvisor.exception.ResourceAlreadyExistsException;
import com.wineadvisor.wineadvisor.exception.ResourceNotFoundException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReviewService {
    ////////////////////////////////
    /////////// VARIABLES //////////
    ////////////////////////////////
    private final ReviewRepository reviewRepository;
    private final WineRepository wineRepository;
    private final UserRepository userRepository;
    private final IdCounterService idCounterService;
    private final MongoTemplate mongoTemplate;
    private final ReviewNeo4jRepository reviewNeo4jRepository;

    /////////// COSTANTI ////////////
    private static final int PAGE_SIZE = 20;


    ////////////////////////////////
    /////// PRIVATE METHODS ////////
    ////////////////////////////////
    
    ////////////////////////////////
    ///// Checking operations //////
    
    // Controlla che la pagina ritornata dalla repo sia valida e che sia consistente rispetto alle opzioni di paginazione richieste dal client.
    private void checkReturnedPage(Page<Review> page, String notFoundMessage) throws ResourceNotFoundException, BadRequestException {
        if (page.getTotalElements() == 0) {
            throw new ResourceNotFoundException(notFoundMessage);
        }
        if (page.getPageable().getPageNumber() >= page.getTotalPages()) {
            throw new BadRequestException("Page requested too high.");
        }
    }

    /// END of checking operations //
    /////////////////////////////////
    

    ////////////////////////////////
    /////// Updates on wines ///////

    // Aggiorna le collection wines, in cui per ogni vintage devono esserci le 3 recensioni più recenti
    private void updateWinesReviews(Long wineId, Integer vintageYear) {
        Optional<Wine> wine_to_find = wineRepository.findByIdAndVintages_Year(wineId, vintageYear);
        if(!wine_to_find.isEmpty()){
            Wine wine = wine_to_find.get();

            // prendo le reviews di quell'annata di quel vino dalla collection delle reviews
            ArrayList<Review> reviews = reviewRepository.findByWineId_IdAndWineId_Year(wineId, vintageYear);

            // ordino le reviews in base alla data di creazione (createdAt) in ordine decrescente e prendo solo le prime 3
            ArrayList<Review> reviews_sorted = sortReviewsByField(reviews, "createdAt", false);
            int limit = Math.min(3, reviews_sorted.size());
            ArrayList<Review> recentReviews = new ArrayList<>(reviews_sorted.subList(0, limit));
            
            // Devo farle diventare del tipo Reviewembedded
            ArrayList<ReviewEmbedded> recentReviewsEmbedded = new ArrayList<>();
            for (int i = 0; i < recentReviews.size(); i++){
                ReviewEmbedded reviewEmbedded = new ReviewEmbedded(
                        recentReviews.get(i).getId(),
                        recentReviews.get(i).getUserId(),
                        recentReviews.get(i).getWineId(),
                        recentReviews.get(i).getRating(),
                        recentReviews.get(i).getText(),
                        recentReviews.get(i).getCreatedAt(),
                        recentReviews.get(i).getLikesCount(),
                        recentReviews.get(i).getDislikesCount()
                );

                recentReviewsEmbedded.add(reviewEmbedded);
            }

            // aggiorno la vintage di quel vino con le (max 3) recensioni più recenti
            for (int i = 0; i < wine.getVintages().size(); i++){
                if (wine.getVintages().get(i).getYear() == null && vintageYear == null){
                    wine.getVintages().get(i).setReviews(recentReviewsEmbedded);
                    break;
                }
                else if (wine.getVintages().get(i).getYear().equals(vintageYear)){
                    wine.getVintages().get(i).setReviews(recentReviewsEmbedded);
                    break;
                }
            }
            wineRepository.save(wine);
        }
    }

    //// END of updates on wines ////
    /////////////////////////////////
    

    /////////////////////////////////
    /////// Updates on users ////////

    // Aggiorna la collection users, in cui per ogni utente devono esserci le 3 recensioni più recenti
    private void updateUsersReviews(String username) {
        Optional<User> user_to_find = userRepository.findByLogin_Username(username);
        if(!user_to_find.isEmpty()){
            User user = user_to_find.get();
            // prendo le reviews di quell'utente dalla collection delle reviews
            ArrayList<Review> reviews = reviewRepository.findByUserId_Username(username);
            // ordino le reviews in base alla data di creazione (createdAt) in ordine decrescente e prendo solo le prime 3
            ArrayList<Review> reviews_sorted = sortReviewsByField(reviews, "createdAt", false);
            int limit = Math.min(3, reviews_sorted.size());
            ArrayList<Review> recentReviews = new ArrayList<>(reviews_sorted.subList(0, limit));

            // Devo farle diventare del tipo ReviewEmbedded
            ArrayList<ReviewEmbedded> recentReviewsEmbedded = new ArrayList<>();
            for (int i = 0; i < recentReviews.size(); i++){
                ReviewEmbedded reviewEmbedded = new ReviewEmbedded(recentReviews.get(i).getId(),
                        recentReviews.get(i).getUserId(),
                        recentReviews.get(i).getWineId(),
                        recentReviews.get(i).getRating(),
                        recentReviews.get(i).getText(),
                        recentReviews.get(i).getCreatedAt(),
                        recentReviews.get(i).getLikesCount(),
                        recentReviews.get(i).getDislikesCount()
                );
                
                recentReviewsEmbedded.add(reviewEmbedded);
            }

            // aggiorno l'utente con le (max 3) recensioni più recenti
            user.setReviews(recentReviewsEmbedded);
            userRepository.save(user);

            // Aggiorno anche Neo4j
            reviewNeo4jRepository.updateRecentReviewsForUser(username, user.getName().getFirst(), user.getName().getLast(), user.getPicture().getThumbnail(), recentReviews);
        }
    }

    //// END of updates on users ////
    /////////////////////////////////
    

    /////////////////////////////////
    /////// Async. operations ///////
    
    // Operazione che una volta al giorno aggiorna le recensioni più recenti di ogni vino e di ogni utente (3 al massimo)
    @Scheduled(cron = "0 0 1 * * ?")   // Ogni giorno all'una di notte
    private void updateReviewsEmbeddedWinesAndUsers() {
        System.out.println("--- INFO: Updating reviews embedded in wines...");
        ArrayList<Wine> wines = (ArrayList<Wine>) wineRepository.findAll();
        for (int i = 0; i < wines.size(); i++){
            ArrayList<Vintage> vintages = wines.get(i).getVintages();
            for (int j = 0; j < vintages.size(); j++){
                updateWinesReviews(wines.get(i).getId(), vintages.get(j).getYear());
            }
        }
        System.out.println("--- INFO: Reviews embedded in wines successfully updated.");

        System.out.println("--- INFO: Updating reviews embedded in users...");
        ArrayList<User> users = (ArrayList<User>) userRepository.findAll();
        for (int i = 0; i < users.size(); i++){
            updateUsersReviews(users.get(i).getLogin().getUsername());
        }
        System.out.println("--- INFO: Reviews embedded in users successfully updated.\n\n");
    }

    //// END of async operations ////
    /////////////////////////////////
    

    /////////////////////////////////
    ///// Aggregation pipelines /////

    // Una volta al mese, aggiorna la top 10 vintages (per popolarità = n° recensioni) per ogni regione
    // @Scheduled(cron = "30 03 17 * * ?")     // Scheduling for debugging purposes.
    @Scheduled(cron = "0 0 2 * * MON")      // Ogni lunedì alle 2 di notte
    private void updateTop10VintagesPerRegion() {
        System.out.println("--- INFO: Declaring aggregation pipeline stages for collection \"regions\".");
        List<Document> pipeline = Arrays.asList(
                new Document("$lookup", new Document("from", "users")
                        .append("localField", "user_id.username")
                        .append("foreignField", "login.username")
                        .append("as", "result")),

                new Document("$unwind", new Document("path", "$result")),

                new Document("$project", new Document("_id", 0)
                        .append("wine_id", "$wine_id.id")
                        .append("wine_name", "$wine_id.name")
                        .append("wine_image", "$wine_id.image")
                        .append("year", "$wine_id.year")
                        .append("region", "$result.address.region")),

                new Document("$group", new Document("_id", new Document("wine_id", "$wine_id")
                        .append("year", "$year")
                        .append("region", "$region"))
                        .append("wine_name", new Document("$first", "$wine_name"))
                        .append("wine_image", new Document("$first", "$wine_image"))
                        .append("count", new Document("$sum", 1))),

                new Document("$sort", new Document("count", -1)),

                new Document("$group", new Document("_id", "$_id.region")
                        .append("vintages", new Document("$push", new Document("wine_id", "$_id.wine_id")
                                .append("wine_name", "$wine_name")
                                .append("year", "$_id.year")
                                .append("bottle", "$wine_image")
                                .append("count", "$count")))),

                new Document("$project", new Document("name", "$_id")
                        .append("vintages", new Document("$slice", Arrays.asList("$vintages", 10)))),

                new Document("$project", new Document("_id", 0)
                        .append("name", 1)
                        .append("top_10_vintages_of_the_week", "$vintages")),

                new Document("$merge", new Document("into", "regions")
                        .append("on", "name")
                        .append("whenMatched", "merge")
                        .append("whenNotMatched", "discard")));
        
        
        // Assicuro che esistano gli indici NECESSARI alla pipeline.
        mongoTemplate
            .indexOps("regions")
            .ensureIndex(
                new Index()
                    .on("name", Sort.Direction.ASC)
                    .named("on_name_UNIQUE")
                    .unique()
            );
        System.out.println("--- INFO: Ensured UNIQUE index on field \"name\" for collection \"regions\".");

        // Assicuro che esistano gli indici FUNZIONALI alla pipeline.
        mongoTemplate
            .indexOps("users")
            .ensureIndex(
                new Index()
                    .on("login.username", Sort.Direction.ASC)
                    .named("on_username_UNIQUE")
                    .unique()
            );
        System.out.println("--- INFO: Ensured INDEX on field \"login.username\" for collection \"users\".");

        System.out.println("--- INFO: Executing aggregation pipeline for collection \"regions\"...");
        mongoTemplate.getCollection("reviews").aggregate(pipeline).allowDiskUse(true).toCollection();

        System.out.println("--- INFO: Collection \"regions\" updated successfully.\n\n");
    }

    // Una volta al mese, aggiorna la top 100 vintages (per popolarità = n° recensioni) per ogni nazione
    // @Scheduled(cron = "30 03 17 * * ?")     // Scheduling for debugging purposes.
    @Scheduled(cron = "0 0 2 * * MON")      // Ogni lunedì alle 2 di notte
    private void updateTop100VintagesPerCountry() {
        System.out.println("--- INFO: Declaring aggregation pipeline stages for collection \"countries\".");
        List<Document> pipeline = Arrays.asList(
                new Document("$lookup",
                        new Document("from", "users")
                                .append("localField", "user_id.username")
                                .append("foreignField", "login.username")
                                .append("as", "result")),

                new Document("$unwind",
                        new Document("path", "$result")),

                new Document("$project",
                        new Document("_id", 0L)
                                .append("wine_id", "$wine_id.id")
                                .append("wine_name", "$wine_id.name")
                                .append("wine_image", "$wine_id.image")
                                .append("year", "$wine_id.year")
                                .append("country", "$result.address.country")),

                new Document("$group",
                        new Document("_id",
                                new Document("wine_id", "$wine_id")
                                        .append("year", "$year")
                                        .append("country", "$country"))
                                .append("wine_name",
                                        new Document("$first", "$wine_name"))
                                .append("wine_image",
                                        new Document("$first", "$wine_image"))
                                .append("count",
                                        new Document("$sum", 1L))),

                new Document("$sort",
                        new Document("count", -1L)),

                new Document("$group",
                        new Document("_id", "$_id.country")
                                .append("vintages",
                                        new Document("$push",
                                                new Document("wine_id", "$_id.wine_id")
                                                        .append("wine_name", "$wine_name")
                                                        .append("year", "$_id.year")
                                                        .append("bottle", "$wine_image")
                                                        .append("count", "$count")))),

                new Document("$project",
                        new Document("_id", 0L)
                                .append("name", "$_id")
                                .append("vintages",
                                        new Document("$slice", Arrays.asList("$vintages", 100L)))),

                new Document("$project", new Document("_id", 0)
                        .append("name", 1)
                        .append("top_100_vintages_of_the_week", "$vintages")),

                new Document("$merge", new Document("into", "countries")
                        .append("on", "name")
                        .append("whenMatched", "merge")
                        .append("whenNotMatched", "discard")));

        
        // Assicuro che esistano gli indici NECESSARI alla pipeline.
        mongoTemplate
            .indexOps("countries")
            .ensureIndex(
                new Index()
                    .on("name", Sort.Direction.ASC)
                    .named("on_name_UNIQUE")
                    .unique()
            );
        System.out.println("--- INFO: Ensured UNIQUE index on field \"name\" for collection \"countries\".");

        // Assicuro che esistano gli indici FUNZIONALI alla pipeline.
        mongoTemplate
            .indexOps("users")
            .ensureIndex(
                new Index()
                    .on("login.username", Sort.Direction.ASC)
                    .named("on_username_UNIQUE")
                    .unique()
            );
        System.out.println("--- INFO: Ensured INDEX on field \"login.username\" for collection \"users\".");

        System.out.println("--- INFO: Executing aggregation pipeline for collection \"countries\"...");
        mongoTemplate.getCollection("reviews").aggregate(pipeline).allowDiskUse(true).toCollection();

        System.out.println("--- INFO: Collection \"countries\" updated successfully.\n\n");
    }

    /// END of aggregation pipelines ///
    ////////////////////////////////////



    /////////////////////////////////
    //////// PUBLIC METHODS /////////
    /////////////////////////////////
    
    /////////////////////////////////
    /////// CRUD operations /////////

    /// CREATE operations ///
    // Aggiunge una recensione alla collection "reviews" del database
    public Review addReview(String username, CreateReviewDTO createdReview) throws ResourceNotFoundException, ResourceAlreadyExistsException {
        // Controllo se l'utente esiste
        User user = userRepository.findByLogin_Username(username)
            .orElseThrow(() -> new ResourceNotFoundException("User with username " + username + " not found."));

        // Controllo se l'utente ha già recensito il vino
        if (reviewRepository.findByUserId_UsernameAndWineId_IdAndWineId_Year(username, createdReview.getWineId(), createdReview.getYear()).isPresent()) {
            throw new ResourceAlreadyExistsException("User with username " + username + " has already reviewed the wine with id " + createdReview.getWineId() + " and year " + createdReview.getYear() + ".");
        }

        Review review = new Review();
        review.setUserId(new UserId());
        review.setWineId(new WineId());

        review.getUserId().setUsername(user.getLogin().getUsername());
        review.getUserId().setThumbnail(user.getPicture().getThumbnail());
        review.getWineId().setId(createdReview.getWineId());
        review.getWineId().setYear(createdReview.getYear());
        review.setRating(createdReview.getRating());
        review.setText(createdReview.getText());

        // Devo controllare che l'utente abbia indicato un vino e un'annata esistenti
        Wine wine = wineRepository.findByIdAndVintages_Year(review.getWineId().getId(), review.getWineId().getYear()).orElseThrow(
            () -> new ResourceNotFoundException("Wine with id " + review.getWineId().getId() + " and year " + review.getWineId().getYear() + " not found.")
        );
        
        review.getWineId().setImage(wine.getVintages().get(0).getImage());
        review.getWineId().setName(wine.getName());
        review.setLikesCount((long) 0);
        review.setDislikesCount((long) 0);
        review.setCreatedAt(Instant.now());
        
        // Setto l'id della recensione
        review.setId(idCounterService.generateSequence("reviews"));

        ReviewEmbedded reviewEmbedded = new ReviewEmbedded(review.getId(), review.getUserId(), review.getWineId(), review.getRating(), review.getText(), review.getCreatedAt(), review.getLikesCount(), review.getDislikesCount());

        // Aggiungo la recensione alla lista delle recensioni dell'utente (collection users)
        if(user.getReviews().size() >= 3) {
            user.getReviews().remove(2);
        }
        user.getReviews().add(0, reviewEmbedded);
        userRepository.save(user);

        Review savedReview = reviewRepository.save(review);

        // Sincronizzazione Neo4j
        reviewNeo4jRepository.createReviewForUser(
                username,
                user.getName().getFirst(),
                user.getName().getLast(),
                savedReview.getId(),
                savedReview.getText(),
                savedReview.getRating(),
                savedReview.getWineId().getId(),
                savedReview.getWineId().getName(),
                savedReview.getWineId().getYear(),
                savedReview.getWineId().getImage(),
                savedReview.getUserId().getThumbnail(),
                savedReview.getCreatedAt()
            );

        return savedReview;

    }


    /// READ operations ///
    // Restituisce tutte le recensioni dalla collection "reviews" del database
    public Page<Review> getAllReviews(Integer page) {
        Page<Review> reviews = reviewRepository.findAll(PageRequest.of(page, PAGE_SIZE));
        checkReturnedPage(reviews, "No reviews found.");
        return reviews;
    }

    // Cerca una recensione per id nella collection "reviews" del database
    public Review getReviewById(Long id) throws ResourceNotFoundException {
        Review review = reviewRepository.findById(id).orElseThrow(
            () -> new ResourceNotFoundException("Review with id " + id + " not found.")
        );

        return review;
    }

    // Restituisce tutte le recensioni di un vino specifico di un'annata specifica dalla collection "reviews" del database
    public Page<Review> getReviewsByVintage(Integer page, Long wineId, Integer vintageYear) {
        Page<Review> reviews = reviewRepository.findByWineId_IdAndWineId_Year(PageRequest.of(page, PAGE_SIZE), wineId, vintageYear);
        checkReturnedPage(reviews, "Reviews for wine with id " + wineId + " and year " + vintageYear + " not found.");
        return reviews;
    }

    // Restituisce tutte le recensioni di un vino specifico dalla collection "reviews" del database
    public Page<Review> getReviewsByWine(Integer page, Long wineId) {
        Page<Review> reviews = reviewRepository.findByWineId_Id(PageRequest.of(page, PAGE_SIZE), wineId);
        checkReturnedPage(reviews, "Reviews for wine with id " + wineId + " not found.");
        return reviews;
    }

    // Restituisce tutte le recensioni di un utente specifico dalla collection "reviews" del database
    public Page<Review> getReviewsByUser(Integer page, String username) {
        Page<Review> reviews = reviewRepository.findByUserId_Username(PageRequest.of(page, PAGE_SIZE), username);
        checkReturnedPage(reviews, "Reviews for user with username " + username + " not found.");
        return reviews;
    }

    // Restituisce tutte le recensioni di un utente specifico per un vino specifico dalla collection "reviews" del database
    public Page<Review> getReviewsByUserAndWine(Integer page, String username, Long wineId) {
        Page<Review> reviews = reviewRepository.findByUserId_UsernameAndWineId_Id(PageRequest.of(page, PAGE_SIZE), username, wineId);
        checkReturnedPage(reviews, "Reviews for user with username " + username + " and wine with id " + wineId + " not found.");
        return reviews;
    }

    // Calcola e restituisce la media dei rating di un vino
    public Double getAverageRatingByWine(Long wineId) throws ResourceNotFoundException {
        if (wineRepository.findById(wineId).isEmpty()) {
            throw new ResourceNotFoundException("Wine with id " + wineId + " not found.");
        }
        ArrayList<Review> reviews = reviewRepository.findByWineId_Id(wineId);
        Double sum = (double) 0;
        for (Review review : reviews) {
            sum += review.getRating();
        }
        return sum / reviews.size();
    }

    // Calcola e restituisce la media dei rating di un'annata di un vino
    public Double getAverageRatingByVintage(Long wineId, Integer year) throws ResourceNotFoundException {
        if (wineRepository.findById(wineId).isEmpty()) {
            throw new ResourceNotFoundException("Wine with id " + wineId + " not found.");
        }
        if (wineRepository.findByIdAndVintages_Year(wineId, year).isEmpty()) {
            throw new ResourceNotFoundException("Wine with id " + wineId + " and year " + year + " not found.");
        }

        ArrayList<Review> reviews = reviewRepository.findByWineId_IdAndWineId_Year(wineId, year);
        Double sum = (double) 0;
        for (Review review : reviews) {
            sum += review.getRating();
        }
        return sum / reviews.size();
    }

    // Restituisce le recensioni di un vino specifico in un range di rating specifico
    public Page<Review> getReviewsByWineAndRatingRange(Integer page, Long wineId, Double minRating, Double maxRating) {
        if (wineRepository.findById(wineId).isEmpty()) {
            throw new ResourceNotFoundException("Wine with id " + wineId + " not found.");
        }
        
        Page<Review> reviews =  reviewRepository.findByWineId_IdAndRatingBetween(PageRequest.of(page, PAGE_SIZE), wineId, minRating, maxRating);
        checkReturnedPage(reviews, "No reviews found for wine with id " + wineId + " in the rating range [" + minRating + ", " + maxRating + "].");
        return reviews;
    }

    // Restituisce le recensioni di un'annata specifica per un determinato vino in un range di rating specifico
    public Page<Review> getReviewsByVintageAndRatingRange(Integer page, Long wineId, Integer year, Double minRating, Double maxRating) {
        if (wineRepository.findById(wineId).isEmpty()) {
            throw new ResourceNotFoundException("Wine with id " + wineId + " not found.");
        }
        if (wineRepository.findByIdAndVintages_Year(wineId, year) == null) {
            throw new ResourceNotFoundException("Wine with id " + wineId + " and year " + year + " not found.");
        }

        Page<Review> reviews = reviewRepository.findByWineId_IdAndWineId_YearAndRatingBetween(PageRequest.of(page, PAGE_SIZE), wineId, year, minRating, maxRating);
        checkReturnedPage(reviews, "No reviews found for wine with id " + wineId + " and year " + year + " in the rating range [" + minRating + ", " + maxRating + "].");
        return reviews;
    }

    // Restituisce le num recensioni più popolari (con più like) di un'annata specifica per un determinato vino
    public ArrayList<Review> getPopularReviewsByVintage(Long wineId, Integer vintageYear, int num) throws ResourceNotFoundException {
        if (wineRepository.findById(wineId).isEmpty()) {
            throw new ResourceNotFoundException("Wine with id " + wineId + " not found.");
        }
        if (wineRepository.findByIdAndVintages_Year(wineId, vintageYear) == null) {
            throw new ResourceNotFoundException("Wine with id " + wineId + " and year " + vintageYear + " not found.");
        }

        ArrayList<Review> reviews = reviewRepository.findByWineId_IdAndWineId_Year(wineId, vintageYear);

        // Controllo se reviews è vuoto
        if (reviews.isEmpty()) {
            throw new ResourceNotFoundException("No reviews found for wine with id " + wineId + " and year " + vintageYear + ".");
        }

        // Ordino le recensioni in base al numero di like (likesCount) in ordine decrescente
        ArrayList<Review> popularReviews = sortReviewsByField(reviews, "likesCount", false);
        
        // Prendo solo il numero richiesto di recensioni, evitando errori di indice
        int limit = Math.min(num, popularReviews.size()); 
        return new ArrayList<>(popularReviews.subList(0, limit));
    }

    public List<Map<String, Object>> getGraphReviewsByUser(String username) {
        return reviewNeo4jRepository.getReviewsByUser(username);
    }

    
    /// UPDATE operations ///
    // Aggiorna una recensione nella collection "reviews" del db
    public Review updateReview(Long id, String username, UpdateReviewDTO updatedReview) throws ResourceNotFoundException, BadRequestException {
        return reviewRepository
            .findByIdAndUserId_Username(id, username)
            .map(
                review -> {
                    // Controllo che rating e testo siano stati modificati
                    if(review.getText().equals(updatedReview.getText()) && review.getRating().equals(updatedReview.getRating())) {
                        throw new BadRequestException("Rating and text are the same as the previous one.");
                    }
                    review.setRating(updatedReview.getRating());
                    review.setText(updatedReview.getText());

                    Wine wine = wineRepository.findByIdAndVintages_Year(review.getWineId().getId(), review.getWineId().getYear()).orElseThrow(
                        () -> new ResourceNotFoundException("Wine with id " + review.getWineId().getId() + " and year " + review.getWineId().getYear() + " not found.")
                    );
                    
                    User user = userRepository.findByLogin_Username(review.getUserId().getUsername()).orElseThrow(
                        () -> new ResourceNotFoundException("User with username " + review.getUserId().getUsername() + " not found.")
                    );

                    // Se dentro a wine è presente la recensione, devo aggiornala
                    ArrayList<Vintage> vintages = wine.getVintages();
                    for (Vintage vintage : vintages) {
                        if (vintage.getYear().equals(review.getWineId().getYear())) {
                            for (ReviewEmbedded embedded : vintage.getReviews()) {
                                if (embedded.getReviewId().equals(id)) {
                                    embedded.setRating(updatedReview.getRating());
                                    embedded.setText(updatedReview.getText());
                                    wineRepository.save(wine);
                                    break;
                                }
                            }
                            break;
                        }
                    }

                    // Se dentro a user è presente la recensione, devo aggiornarla
                    for (ReviewEmbedded embedded : user.getReviews()) {
                        if (embedded.getReviewId().equals(id)) {
                            embedded.setRating(updatedReview.getRating());
                            embedded.setText(updatedReview.getText());
                            userRepository.save(user);
                            break;
                        }
                    }

                    Review savedReview = reviewRepository.save(review);

                    reviewNeo4jRepository.updateReview(id, updatedReview.getText(), updatedReview.getRating());
                    
                    return savedReview;
                }).orElseThrow(() -> new ResourceNotFoundException("Review with id " + id +  " and with username " + username + " not found."));
    }

    
    /// DELETE operations ///
    // Cancella tutte le recensioni dalla collection "reviews"
    public void deleteAllReviews() {
        reviewRepository.deleteAll();

        // Cancellazione da Neo4j
        reviewNeo4jRepository.deleteAllReviews();
    }

    // Cancella la recensione con un id sepcifico
    public void deleteReviewById(Long id, String username, String role) throws ResourceNotFoundException, AccessDeniedException {
        // Controllo se la recensione esiste
        Review targetReview = reviewRepository.findById(id).orElseThrow(
            () -> new ResourceNotFoundException("Review with id " + id + " not found.")
        );

        // Controllo che sia stato username a scrivere la recensione, a meno che non sia admin
        System.out.println("--- DBG: role: \"" + role + "\".");
        if (!role.equals("ROLE_ADMIN")) {
            if (!targetReview.getUserId().getUsername().equals(username)) {
                throw new AccessDeniedException();
            }
        }

        // Rimuovo la recensione dal vino (se presente)
        Wine wine = wineRepository.findByVintages_Reviews_ReviewId(id).orElse(null);
        if(wine != null){
            for (Vintage vintage : wine.getVintages()) {
                if (vintage.getReviews().removeIf(r -> r.getReviewId().equals(id))) {
                    wineRepository.save(wine);
                    break;
                }
            }
        }

        // Rimuovo la recensione dall'utente (se presente)
        User user_to_find = userRepository.findByReviews_ReviewId(id).orElse(null);
        if(user_to_find != null) {
            for (ReviewEmbedded review : user_to_find.getReviews()) {
                if (review.getReviewId().equals(id)) {
                    user_to_find.getReviews().remove(review);
                    userRepository.save(user_to_find);
                    break;
                }
            }
        }

        // Rimuovo l'id della recensione dagli array likes/dislikes di users
        ArrayList<User> users = userRepository.findByLikesOrDislikes(id, id);
        for (User user : users) {
            user.getLikes().removeIf(l -> l.equals(id));
            user.getDislikes().removeIf(d -> d.equals(id));
            userRepository.save(user);
        }

        reviewRepository.deleteById(id);

        // Cancellazione da Neo4j
        reviewNeo4jRepository.deleteReviewById(id);
    }

    // Cancella tutte le recensioni di un utente specifico
    public void deleteReviewsByUser(String username) throws ResourceNotFoundException {
        // Controllo se l'utente esiste
        User user = userRepository.findByLogin_Username(username).orElseThrow(
            () -> new ResourceNotFoundException("User with username " + username + " not found.")
        );

        // Controllo se esistono recensioni fatte da quell'utente
        if (reviewRepository.findByUserId_Username(username).isEmpty()) {
            throw new ResourceNotFoundException("Reviews with username " + username + " not found.");
        }

        // Rimuovo gli id delle recensioni dagli array likes/dislikes di users
        ArrayList<ReviewEmbedded> reviews = user.getReviews();
        for (ReviewEmbedded review : reviews) {
            ArrayList<User> users = userRepository.findByLikesOrDislikes(review.getReviewId(), review.getReviewId());
            for (User user1 : users) {
                user1.getLikes().removeIf(l -> l.equals(review.getReviewId()));
                user1.getDislikes().removeIf(d -> d.equals(review.getReviewId()));
                userRepository.save(user1);
            }
        }

        // Rimuovo tutte le recensioni dell'utente dalla lista embedded nel suo oggetto
        user.getReviews().clear();
        userRepository.save(user);

        // Rimuovo le recensioni dell’utente anche dai vini
        List<Wine> wines = wineRepository.findAll();
        for (Wine wine : wines) {
            for (Vintage vintage : wine.getVintages()) {
                vintage.getReviews().removeIf(r ->
                    r.getUserId() != null &&
                    r.getUserId().getUsername().equals(username)
                );
            }
            wineRepository.save(wine);
        }

        reviewRepository.deleteAllByUserId_Username(username);

        // Cancellazione da Neo4j
        reviewNeo4jRepository.deleteAllReviewsByUser(username);
    }

    // Cancella tutte le recensioni di un vino specifico
    public void deleteReviewsByWine(Long wineId) throws ResourceNotFoundException {
        // Controllo se il vino esiste
        Wine wine = wineRepository.findById(wineId)
            .orElseThrow(() -> new ResourceNotFoundException("Wine with id " + wineId + " not found."));

        // Controllo se esistono recensioni per quel vino
        ArrayList<Review> reviews = reviewRepository.findByWineId_Id(wineId);
        if (reviews.isEmpty()){
            throw new ResourceNotFoundException("Reviews with wineId " + wineId + " not found.");
        }

        // Se le recensioni da rimuovere sono presenti anche in un utente o in un wine, devo rimuoverle
        for (Vintage vintage : wine.getVintages()) {
            vintage.getReviews().removeIf(r -> r.getWineId().getId().equals(wineId));
        }
        wineRepository.save(wine);

        // Devo rimuoverle anche dalla collection users (anche dagli array likes/dislikes)
        ArrayList<User> users = (ArrayList<User>) userRepository.findAll();
        for (User user : users) {
            user.getReviews().removeIf(r -> r.getWineId().getId().equals(wineId));
            for (Review review : reviews) {
                user.getLikes().removeIf(l -> l.equals(review.getId()));
                user.getDislikes().removeIf(d -> d.equals(review.getId()));
            }
            userRepository.save(user);
        }

        reviewRepository.deleteAllByWineId_Id(wineId);

        // Cancellazione da Neo4j
        reviewNeo4jRepository.deleteAllReviewsByWine(wineId);
    }

    // Cancella tutte le recensioni di un'annata specifica di un vino specifico
    public void deleteReviewsByVintage(Long wineId, Integer vintageYear) throws ResourceNotFoundException {
        // Controllo se il vino e la vintage esistono
        Wine wine = wineRepository.findById(wineId).orElseThrow(
            () -> new ResourceNotFoundException("Wine with id " + wineId + " not found.")
        );

        if (wineRepository.findByIdAndVintages_Year(wineId, vintageYear).isEmpty()) {
            throw new ResourceNotFoundException("Wine with id " + wineId + " and year " + vintageYear + " not found.");
        }

        // Rimuovo le recensioni dalla vintage nella collection wines
        for (Vintage vintage : wine.getVintages()) {
            if(vintage.getYear().equals(vintageYear)) {
                vintage.getReviews().clear();
                break;
            }
        }
        wineRepository.save(wine);

        // Devo rimuoverle anche dalla collection users
        ArrayList<User> users = (ArrayList<User>) userRepository.findAll();
        for (User user : users) {
            user.getReviews().removeIf(r -> r.getWineId().getId().equals(wineId) && r.getWineId().getYear().equals(vintageYear));
            for (Review review : reviewRepository.findByWineId_IdAndWineId_Year(wineId, vintageYear)) {
                user.getLikes().removeIf(l -> l.equals(review.getId()));
                user.getDislikes().removeIf(d -> d.equals(review.getId()));
            }
            userRepository.save(user);
        }
        
        reviewRepository.deleteAllByWineId_IdAndWineId_Year(wineId, vintageYear);

        // Cancellazione da Neo4j
        reviewNeo4jRepository.deleteAllReviewsByVintage(wineId, vintageYear);
    }


    //// END of crud operations ////
    ////////////////////////////////
    

    ////////////////////////////////
    /// Utility public functions ///
    
    // Ordina e restituisce le recensioni ordinate sulla base del campo specificato, in ordine crescente o decrescente (terzo parametro)
    public ArrayList<Review> sortReviewsByField(ArrayList<Review> reviews, String field, boolean ascendingOrder) {
        reviews.sort((r1, r2) -> {
            switch (field) {
                case "rating":
                    return ascendingOrder ? Double.compare(r1.getRating(), r2.getRating()) : Double.compare(r2.getRating(), r1.getRating());
                case "createdAt":
                    return ascendingOrder ? r1.getCreatedAt().compareTo(r2.getCreatedAt()) : r2.getCreatedAt().compareTo(r1.getCreatedAt());
                case "likesCount":
                    return ascendingOrder ? Long.compare(r1.getLikesCount(), r2.getLikesCount()) : Long.compare(r2.getLikesCount(), r1.getLikesCount());
                case "dislikesCount":
                    return ascendingOrder ? Long.compare(r1.getDislikesCount(), r2.getDislikesCount()) : Long.compare(r2.getDislikesCount(), r1.getDislikesCount());
                case "username":
                    return ascendingOrder ? r1.getUserId().getUsername().compareTo(r2.getUserId().getUsername()) : r2.getUserId().getUsername().compareTo(r1.getUserId().getUsername());
                case "wineId":
                    return ascendingOrder ? r1.getWineId().getId().compareTo(r2.getWineId().getId()) : r2.getWineId().getId().compareTo(r1.getWineId().getId());
                default:
                    return 0;
            }
        });

        return reviews;
    }

    /// END of util. pub. funct. ///
    ////////////////////////////////
}
