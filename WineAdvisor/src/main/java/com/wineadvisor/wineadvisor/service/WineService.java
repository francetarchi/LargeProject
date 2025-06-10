package com.wineadvisor.wineadvisor.service;

import java.util.List;
import java.util.Optional;
import java.util.Arrays;
import java.util.ArrayList;
import java.time.Instant;

import org.bson.BsonNull;
import org.bson.Document;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import com.mongodb.client.AggregateIterable;

import com.wineadvisor.wineadvisor.model.reviews.Review;
import com.wineadvisor.wineadvisor.model.styles.Style;
import com.wineadvisor.wineadvisor.model.users.User;
import com.wineadvisor.wineadvisor.model.utils.ReviewEmbedded;
import com.wineadvisor.wineadvisor.model.wineries.Winery;
import com.wineadvisor.wineadvisor.model.countries.Country;
import com.wineadvisor.wineadvisor.model.wines.Wine;
import com.wineadvisor.wineadvisor.model.wines.fields.CountryEmbedded;
import com.wineadvisor.wineadvisor.model.wines.fields.RegionEmbedded;
import com.wineadvisor.wineadvisor.model.wines.fields.Statistics;
import com.wineadvisor.wineadvisor.model.wines.fields.StyleEmbedded;
import com.wineadvisor.wineadvisor.model.wines.fields.Vintage;
import com.wineadvisor.wineadvisor.model.wines.fields.WineryEmbedded;
import com.wineadvisor.wineadvisor.repository.ReviewRepository;
import com.wineadvisor.wineadvisor.repository.StyleRepository;
import com.wineadvisor.wineadvisor.repository.UserRepository;
import com.wineadvisor.wineadvisor.repository.WineRepository;
import com.wineadvisor.wineadvisor.repository.WineryRepository;
import com.wineadvisor.wineadvisor.repository.CountryRepository;
import com.wineadvisor.wineadvisor.DTO.wines.CreateWineDTO;
import com.wineadvisor.wineadvisor.DTO.wines.NewVintageDTO;
import com.wineadvisor.wineadvisor.DTO.wines.UpdateVintageDTO;
import com.wineadvisor.wineadvisor.DTO.wines.UpdateWineDTO;
import com.wineadvisor.wineadvisor.exception.AccessDeniedException;
import com.wineadvisor.wineadvisor.exception.BadRequestException;
import com.wineadvisor.wineadvisor.exception.ResourceAlreadyExistsException;
import com.wineadvisor.wineadvisor.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class WineService {
    ////////////////////////////////
    /////////// VARIABLES //////////
    ////////////////////////////////
    private final ReviewRepository reviewRepository;
    private final WineRepository wineRepository;
    private final UserRepository userRepository;
    private final WineryRepository wineryRepository;
    private final CountryRepository countryRepository;
    private final StyleRepository styleRepository;

    private final IdCounterService idCounterService;

    private final MongoTemplate mongoTemplate;

    /////////// COSTANTI ////////////
    private static final int PAGE_SIZE = 20;



    ////////////////////////////////
    /////// PRIVATE METHODS ////////
    ////////////////////////////////
    
    ////////////////////////////////
    ///// Checking operations //////
    
    // Controlla che la pagina ritornata dalla repo sia valida e che sia consistente rispetto alle opzioni di paginazione richieste dal client.
    private void checkReturnedPage(Page<Wine> page, String notFoundMessage) throws ResourceNotFoundException, BadRequestException {
        if (page.getTotalElements() == 0) {
            throw new ResourceNotFoundException(notFoundMessage);
        }
        if (page.getPageable().getPageNumber() >= page.getTotalPages()) {
            throw new BadRequestException("Page requested too high.");
        }
    }

    /// END of checking operations //
    /////////////////////////////////
    

    /////////////////////////////////
    /////// Async. operations ///////

    // Aggiorna una volta al giorno l'oggetto statistics di ogni vintage nella collection wines
    @Transactional
    // @Scheduled(cron = "00 23 21 * * ?")    // Scheduling for debugging purposes.
    @Scheduled(cron = "0 0 0 * * ?")    // Ogni giorno a mezzanotte
    protected void updateStatisticsPerVintage() {
        System.out.println("--- INFO: Declaring aggregation pipeline stages for vintage statistics update.");
        List<Document> pipeline = Arrays.asList(
                new Document("$unwind", "$vintages"),
                new Document("$lookup",
                        new Document("from", "reviews")
                                .append("let",
                                        new Document("wineId", "$_id")
                                                .append("vintageYear", "$vintages.year"))
                                .append("pipeline", Arrays.asList(new Document("$match",
                                        new Document("$expr",
                                                new Document("$and", Arrays.asList(
                                                        new Document("$eq", Arrays.asList("$wine_id.id", "$$wineId")),
                                                        new Document("$eq",
                                                                Arrays.asList("$wine_id.year", "$$vintageYear")))))),
                                        new Document("$project",
                                                new Document("rating", 1L))))
                                .append("as", "matched_reviews")),
                new Document("$project",
                        new Document("_id", 1L)
                                .append("year", "$vintages.year")
                                .append("statistics",
                                        new Document("ratings_count",
                                                new Document("$size", "$matched_reviews"))
                                                .append("ratings_average",
                                                        new Document("$cond", Arrays.asList(
                                                                new Document("$gt",
                                                                        Arrays.asList(new Document("$size",
                                                                                "$matched_reviews"), 0L)),
                                                                new Document("$avg", "$matched_reviews.rating"),
                                                                new BsonNull()))))));


        // Assicuro che esistano gli indici FUNZIONALI alla pipeline.
        mongoTemplate
            .indexOps("reviews")
            .ensureIndex(
                new Index()
                    .on("wine_id.id", Sort.Direction.ASC)
                    .on("wine_id.year", Sort.Direction.ASC)
                    .named("index_for_vintage_statistics_update")
            );
        System.out.println("--- INFO: Ensured index on fields \"wine_id.id\" and \"wine_id.year\" for collection \"reviews\".");


        System.out.println("--- INFO: Executing aggregation pipeline for vintage statistics update...");
        AggregateIterable<Document> results = mongoTemplate.getCollection("wines").aggregate(pipeline).allowDiskUse(true);
        System.out.println("--- INFO: Aggregation pipeline executed successfully.");


        System.out.println("--- INFO: Processing results from aggregation pipeline...");
        for (Document doc : results) {
            Object idObj = doc.get("_id");
            Object yearObj = doc.get("year");
            Document stats = (Document) doc.get("statistics");

            // Verifico che tutti i campi siano presenti
            if (idObj == null || yearObj == null || stats == null) {
                continue;
            }

            Long wineId = ((Number) idObj).longValue();
            int year = ((Number) yearObj).intValue();

            // Query per selezionare il vino e la vintage giusta
            Query query = new Query(Criteria.where("_id").is(wineId)
                .and("vintages.year").is(year));

            // Update per sovrascrivere il campo statistics nella vintage corretta
            Update update = new Update().set("vintages.$.statistics", stats);

            mongoTemplate.updateFirst(query, update, "wines");
        }

        System.out.println("--- INFO: Vintage statistics updated successfully.\n\n");
    }

    // Aggiorna una volta al giorno l'oggetto statistics di ogni wine
    // @Scheduled(cron = "00 23 21 * * ?")    // Scheduling for debugging purposes.
    @Scheduled(cron = "0 0 0 * * ?")    // Ogni giorno a mezzanotte
    protected void updateStatisticsPerWine() {
        System.out.println("--- INFO: Declaring aggregation pipeline stages for wine statistics update.");
        List<Document> pipeline = Arrays.asList(
                new Document("$project",
                        new Document("_id", 1L)),
                new Document("$lookup",
                        new Document("from", "reviews")
                                .append("localField", "_id")
                                .append("foreignField", "wine_id.id")
                                .append("as", "matched_reviews")),
                new Document("$project",
                        new Document("_id", 1L)
                                .append("statistics",
                                        new Document("ratings_count",
                                                new Document("$size", "$matched_reviews"))
                                                .append("ratings_average",
                                                        new Document("$cond",
                                                                Arrays.asList(
                                                                        new Document("$gt",
                                                                                Arrays.asList(new Document("$size",
                                                                                        "$matched_reviews"), 0L)),
                                                                        new Document("$avg", "$matched_reviews.rating"),
                                                                        new BsonNull()))))),
                new Document("$merge",
                        new Document("into", "wines")
                                .append("on", "_id")
                                .append("whenMatched", Arrays.asList(new Document("$set",
                                        new Document("statistics", "$$new.statistics"))))
                                .append("whenNotMatched", "discard")));


        // Assicuro che esistano gli indici FUNZIONALI alla pipeline.
        mongoTemplate
            .indexOps("reviews")
            .ensureIndex(
                new Index()
                    .on("wine_id.id", Sort.Direction.ASC)
                    .named("index_for_wine_statistics_update")
            );
        System.out.println("--- INFO: Ensured index on field \"wine_id.id\" for collection \"reviews\".");


        System.out.println("--- INFO: Executing aggregation pipeline for wine statistics update...");
        mongoTemplate.getCollection("wines").aggregate(pipeline).allowDiskUse(true).toCollection();
        System.out.println("--- INFO: Wine statistics updated successfully.\n\n");
    }

    // Aggiorna una volta al giorno il campo wines_count contenuto in grapes, in style, nella collection "wines"
    // Aggiorna anche il campo wines_count contenuto in grapes nella collection "styles"
    @Transactional
    // @Scheduled(cron = "00 23 21 * * ?")    // Scheduling for debugging purposes.
    @Scheduled(cron = "0 0 0 * * ?")    // Ogni giorno a mezzanotte
    protected void updateWinesCountInGrapesPerWine() {
        System.out.println("--- INFO: Declaring aggregation pipeline stages for grapes count update.");
        List<Document> pipeline = Arrays.asList(
                new Document("$unwind",
                        new Document("path", "$style.grapes")),
                new Document("$group",
                        new Document("_id", "$style.grapes.name")
                                .append("wines_count",
                                        new Document("$sum", 1L))));


        System.out.println("--- INFO: Executing aggregation pipeline for grapes count update...");
        AggregateIterable<Document> results = mongoTemplate.getCollection("wines").aggregate(pipeline).allowDiskUse(true);
        System.out.println("--- INFO: Aggregation pipeline executed successfully.");


        System.out.println("--- INFO: Processing results from aggregation pipeline...");
        for (Document doc : results) {
            String grapeName = doc.getString("_id");
            Long winesCount = doc.getLong("wines_count");
        
            if (grapeName == null || winesCount == null) {
                continue;
            }
        
            // Seleziona tutti i vini che contengono questa grape
            Query query_on_wines = new Query(Criteria.where("style.grapes.name").is(grapeName));
            Query query_on_styles = new Query(Criteria.where("grapes.name").is(grapeName));
        
            // Aggiorna ogni entry dentro style.grapes con quel nome
            Update update_on_wines = new Update().set("style.grapes.$[g].wines_count", winesCount);
            Update update_on_styles = new Update().set("grapes.$[g].wines_count", winesCount);
        
            // Definisce il filtro per gli array ("arrayFilters")
            update_on_wines.filterArray("g.name", grapeName);
            update_on_styles.filterArray("g.name", grapeName);
        
            // Applica l'update su tutti i documenti che contengono quella grape
            mongoTemplate.updateMulti(query_on_wines, update_on_wines, "wines");
            mongoTemplate.updateMulti(query_on_styles, update_on_styles, "styles");
        }

        System.out.println("--- INFO: Grapes count updated successfully.\n\n");
    }

    /// END of async. operations ///
    ////////////////////////////////


    
    /////////////////////////////////
    //////// PUBLIC METHODS /////////
    /////////////////////////////////
    
    /////////////////////////////////
    /////// CRUD operations /////////

    /// CREATE operations ///
    // Aggiunge un nuovo vino alla collection wines
    public Wine addWine(CreateWineDTO createWineDTO, String wineryUsername) throws ResourceNotFoundException {
        // Controllo che esista la winery
        Winery winery = wineryRepository.findByLogin_Username(wineryUsername).orElseThrow(
            () -> new ResourceNotFoundException("Winery not found with username: " + wineryUsername + ".")
        );
        
        // Controllo che esista il country nella collection country
        Country country = countryRepository.findByName(winery.getCountry()).orElseThrow(
            () -> new ResourceNotFoundException("Country not found with name: " + winery.getCountry() + ".")
        );

        Wine wine = new Wine();
        
        wine.setId(idCounterService.generateSequence("wines"));
        wine.setName(createWineDTO.getName());
        wine.setType(createWineDTO.getType());
        wine.setIsNatural(createWineDTO.getIsNatural());

        wine.setTaste(null);

        Optional<Style> style_to_find = styleRepository.findByName(createWineDTO.getStyle());
        if(style_to_find.isEmpty()) {
            wine.setStyle(null);
        } else {
            Style style = style_to_find.get();

            wine.setStyle(new StyleEmbedded());
            wine.getStyle().setName(style.getName());
            wine.getStyle().setDescription(style.getDescription());
            wine.getStyle().setInterestingFacts(style.getInterestingFacts());
            wine.getStyle().setFood(style.getFood());
            wine.getStyle().setGrapes(style.getGrapes());
        }
        

        wine.setVintages(new ArrayList<Vintage>());
        wine.setStatistics(new Statistics());
        wine.getStatistics().setRatingsAverage(0.0);
        wine.getStatistics().setRatingsCount((long) 0);

        wine.setWinery(new WineryEmbedded());
        wine.getWinery().setName(winery.getName());
        wine.getWinery().setUsername(winery.getLogin().getUsername());
        wine.getWinery().setThumbnail(winery.getPicture().getThumbnail());
        
        wine.setRegion(new RegionEmbedded());
        wine.getRegion().setName(winery.getRegion());
        wine.getRegion().setCountry(new CountryEmbedded());
        wine.getRegion().getCountry().setName(country.getName());
        wine.getRegion().getCountry().setCurrency(country.getCurrency());

        wine.setCreatedAt(Instant.now());

        return wineRepository.save(wine);
    }


    /// READ operations ///
    // Restituisce tutti i vini (con paginazione)
    public Page<Wine> getAllWines(Integer page) {
        Page<Wine> wines = wineRepository.findAll(PageRequest.of(page, PAGE_SIZE));
        checkReturnedPage(wines, "No wines found.");
        return wines;
    }

    // Restituisce un vino per id
    public Wine getWineById(Long wineId) throws ResourceNotFoundException {
        Wine wine = wineRepository.findById(wineId).orElseThrow(
            () -> new ResourceNotFoundException("Wine with id " + wineId + " not found.")
        );

        return wine;
    }

    // Restituisce un'annata di un vino specifico
    public Vintage getVintage(Long wineId, Integer year) throws ResourceNotFoundException {
        Wine wine = wineRepository.findByIdAndVintages_Year(wineId, year).orElseThrow(
            () -> new ResourceNotFoundException("Vintage with wineId " + wineId + " and year " + year + " not found.")
        );
        
        for (Vintage vintage : wine.getVintages()) {
            if (vintage.getYear().equals(year)) return vintage;
        }

        return null;
    }

    // Restituisce vini sulla base dei filtri di ricerca indicati 
    public Page<Wine> searchWines(
        Integer page,
        String name,
        String winery,
        String region,
        String country,
        String type,
        String grape,
        Double min,
        Double max,
        Double minAverageRating) {

        Page<Wine> wines = wineRepository.findByNameContainingIgnoreCaseAndWinery_UsernameContainingIgnoreCaseAndRegion_NameContainingIgnoreCaseAndRegion_Country_NameContainingIgnoreCaseAndTypeContainingIgnoreCaseAndStyle_Grapes_NameContainingIgnoreCaseAndStatistics_RatingsAverageGreaterThanEqualAndVintages_PriceBetween(PageRequest.of(page, PAGE_SIZE), name, winery, region, country, type, grape, minAverageRating, min, max);
        checkReturnedPage(wines, "No wines found with the specified filters.");
        return wines;
    }


    /// UPDATE operations ///
    // Modifica dati del vino
    @Transactional
    public Wine updateWine(Long wineId, UpdateWineDTO updatedWine, String username) throws ResourceNotFoundException {
        return wineRepository.findById(wineId)
            .map(wine -> {
                // Controllo che il vino sia della winery username
                if(wineRepository.findByIdAndWinery_Username(wineId, username).isEmpty()){
                    throw new ResourceNotFoundException("Winery " + username + " does not own wine with id " + wineId + ".");
                }
                
                wine.setName(updatedWine.getName());
                wine.setType(updatedWine.getType());
                wine.setIsNatural(updatedWine.getIsNatural());

                Optional<Style> style_to_find = styleRepository.findByName(updatedWine.getStyle());
                if(style_to_find.isEmpty()) {
                    wine.setStyle(null);
                } else {
                    Style style = style_to_find.get();

                    wine.setStyle(new StyleEmbedded());
                    wine.getStyle().setName(style.getName());
                    wine.getStyle().setDescription(style.getDescription());
                    wine.getStyle().setInterestingFacts(style.getInterestingFacts());
                    wine.getStyle().setFood(style.getFood());
                    wine.getStyle().setGrapes(style.getGrapes());
                }

                // Devo aggiornare il nome anche nelle reviews embedded nella collection users
                ArrayList<User> users = userRepository.findByReviews_WineId_Id(wineId);
                if (!users.isEmpty()){
                    for (User user : users){
                        for (ReviewEmbedded review : user.getReviews()){
                            if (review.getWineId().getId().equals(wineId)){
                                review.getWineId().setName(updatedWine.getName());
                            }
                        }
                        userRepository.save(user);
                    }
                }

                // Devo aggiornare il nome anche nelle reviews della collection reviews
                ArrayList<Review> reviews = reviewRepository.findByWineId_Id(wineId);
                if (!reviews.isEmpty()){
                    for (Review review : reviews){
                        review.getWineId().setName(updatedWine.getName());
                        reviewRepository.save(review);
                    }
                }

                return wineRepository.save(wine);
            })
            .orElseThrow(
                () -> new ResourceNotFoundException("Wine with id " + wineId + " not found.")
            );
    }

    // Aggiunge una nuova annata ad un determinato vino
    public Wine addVintage(NewVintageDTO newVintage, String username) throws ResourceNotFoundException, ResourceAlreadyExistsException {
        return wineRepository.findById(newVintage.getWineId())
            .map(wine -> {
                // Controllo che il vino sia della winery username
                if(wineRepository.findByIdAndWinery_Username(newVintage.getWineId(), username).isEmpty()){
                    throw new ResourceNotFoundException("Winery " + username + " does not own wine with id " + newVintage.getWineId() + ".");
                }

                // Controllo che la vintage non esista già
                if (wineRepository.findByIdAndVintages_Year(newVintage.getWineId(), newVintage.getYear()).isPresent()){
                    throw new ResourceAlreadyExistsException("Vintage with wineId " + newVintage.getWineId() + " and year " + newVintage.getYear() + " already exists.");
                }

                Vintage vintage = new Vintage();

                vintage.setYear(newVintage.getYear());
                vintage.setPrice(newVintage.getPrice());
                vintage.setImage(newVintage.getImage());

                vintage.setStatistics(new Statistics());
                vintage.getStatistics().setRatingsAverage(0.0);
                vintage.getStatistics().setRatingsCount((long) 0);

                vintage.setReviews(new ArrayList<ReviewEmbedded>());

                vintage.setCreatedAt(Instant.now());

                wine.getVintages().add(vintage);

                return wineRepository.save(wine);
            })
            .orElseThrow(
                () -> new ResourceNotFoundException("Wine with id " + newVintage.getWineId() + " not found.")
            );
    }

    // Modifica dati di una vintage di un vino
    @Transactional
    public Wine updateVintage(UpdateVintageDTO updatedVintage, String username) throws ResourceNotFoundException {
        return wineRepository.findByIdAndVintages_Year(updatedVintage.getWineId(), updatedVintage.getYear())
            .map(wine -> {  
                // Controllo che il vino sia della winery username
                if(wineRepository.findByIdAndWinery_Username(updatedVintage.getWineId(), username).isEmpty()){
                    throw new ResourceNotFoundException("Winery " + username + " does not own wine with id " + updatedVintage.getWineId() + ".");
                }
                
                for (Vintage vintage : wine.getVintages()){
                    if (vintage.getYear().equals(updatedVintage.getYear())){
                        vintage.setPrice(updatedVintage.getPrice());
                        vintage.setImage(updatedVintage.getImage());
                        break;
                    }
                }

                // Bisogna aggiornare la image anche nelle reviews embedded in users
                ArrayList<User> users = userRepository.findByReviews_WineId_IdAndReviews_WineId_Year(updatedVintage.getWineId(), updatedVintage.getYear());
                if (!users.isEmpty()){
                    for (User user : users){
                        for (ReviewEmbedded review : user.getReviews()){
                            if (review.getWineId().getId().equals(updatedVintage.getWineId()) && review.getWineId().getYear().equals(updatedVintage.getYear())){
                                review.getWineId().setImage(updatedVintage.getImage());
                            }
                        }
                        userRepository.save(user);
                    }
                }

                // Bisogna aggiornare la image anche nelle reviews della collection reviews
                ArrayList<Review> reviews = reviewRepository.findByWineId_IdAndWineId_Year(updatedVintage.getWineId(), updatedVintage.getYear());
                if (!reviews.isEmpty()){
                    for (Review review : reviews){
                        review.getWineId().setImage(updatedVintage.getImage());
                        reviewRepository.save(review);
                    }
                }

                return wineRepository.save(wine);
            })
            .orElseThrow(
                () -> new ResourceNotFoundException("Vintage with wineId " + updatedVintage.getWineId() + " and year " + updatedVintage.getYear() +" not found.")
            );
    }

    // Elimina una vintage da un vino
    @Transactional
    public Wine deleteVintage(UpdateVintageDTO targetVintage, String username) throws ResourceNotFoundException, AccessDeniedException {
        return wineRepository
            .findByIdAndVintages_Year(targetVintage.getWineId(), targetVintage.getYear())
            .map(
                wine -> {
                    // Controllo che il vino appartenga alla winery username
                    if(!wine.getWinery().getUsername().equals(username)) {
                        throw new AccessDeniedException();
                    }

                    ArrayList<Review> reviews_to_delete = reviewRepository.findByWineId_IdAndWineId_Year(targetVintage.getWineId(), targetVintage.getYear());
                    ArrayList<User> users = (ArrayList<User>) userRepository.findAll();
                    for (User user : users){
                        boolean modified = user.getReviews().removeIf(r -> r.getWineId().getId().equals(targetVintage.getWineId()) && r.getWineId().getYear().equals(targetVintage.getYear()));

                        if (user.getLikes().removeIf(likeId ->
                                reviews_to_delete.stream().anyMatch(r -> r.getId().equals(likeId)))) {
                            modified = true;
                        }

                        if (user.getDislikes().removeIf(dislikeId ->
                                reviews_to_delete.stream().anyMatch(r -> r.getId().equals(dislikeId)))) {
                            modified = true;
                        }

                        if (modified) {
                            userRepository.save(user);
                        }
                    }

                    reviewRepository.deleteAllByWineId_IdAndWineId_Year(targetVintage.getWineId(), targetVintage.getYear());
                    
                    wine.getVintages().removeIf(vintage -> vintage.getYear().equals(targetVintage.getYear()));
                    return wineRepository.save(wine);
                }
            )
            .orElseThrow(
                () -> new ResourceNotFoundException("Vintage with wineId " + targetVintage.getWineId() + " and year " + targetVintage.getYear() + " not found.")
            );
    }


    /// DELETE operations ///
    // Elimina tutti i vini dalla collection "wines"
    public void deleteAllWines(){
        wineRepository.deleteAll();
    }

    // Elimina un vino in base al suo id
    @Transactional
    public void deleteWineById(Long wineId, String username) throws ResourceNotFoundException {
        // Controllo che il vino specificato esista
        if (wineRepository.findById(wineId).isEmpty()){
            throw new ResourceNotFoundException("Wine with id " + wineId + " not found.");
        }

        // Controllo che il vino sia della winery username
        if(wineRepository.findByIdAndWinery_Username(wineId, username).isEmpty()){
            throw new ResourceNotFoundException("Winery " + username + " does not own wine with id " + wineId + ".");
        }
        
        // Se elimino un vino, devo eliminare anche tutte le recensioni fatte su quel vino
        // e togliere l'id di quelle recensioni dagli array "likes"/"dislikes" degli users.
        ArrayList<Review> reviews_to_delete = reviewRepository.findByWineId_Id(wineId);
        ArrayList<User> users = (ArrayList<User>) userRepository.findAll();
        for (User user : users){
            boolean modified = user.getReviews().removeIf(r -> r.getWineId().getId().equals(wineId));
            if (user.getLikes().removeIf(likeId -> reviews_to_delete.stream().anyMatch(r -> r.getId().equals(likeId)))) {
                modified = true;
            }
            if (user.getDislikes().removeIf(dislikeId -> reviews_to_delete.stream().anyMatch(r -> r.getId().equals(dislikeId)))) {
                modified = true;
            }
            if (modified) {
                userRepository.save(user);
            }
        }

        reviewRepository.deleteAllByWineId_Id(wineId);
        wineRepository.deleteById(wineId);
    }

    //// END of crud operations ////
    ////////////////////////////////
}
