package com.wineadvisor.wineadvisor.service;

import org.bson.BsonNull;
import org.bson.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mongodb.client.AggregateIterable;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Scheduled;

import com.wineadvisor.wineadvisor.model.reviews.Review;
import com.wineadvisor.wineadvisor.model.users.User;
import com.wineadvisor.wineadvisor.model.utils.ReviewEmbedded;
import com.wineadvisor.wineadvisor.model.wineries.Winery;
import com.wineadvisor.wineadvisor.model.countries.Country;
import com.wineadvisor.wineadvisor.model.wines.Wine;
import com.wineadvisor.wineadvisor.model.wines.fields.BaselineStructure;
import com.wineadvisor.wineadvisor.model.wines.fields.CountryEmbedded;
import com.wineadvisor.wineadvisor.model.wines.fields.Food;
import com.wineadvisor.wineadvisor.model.wines.fields.Grape;
import com.wineadvisor.wineadvisor.model.wines.fields.RegionEmbedded;
import com.wineadvisor.wineadvisor.model.wines.fields.Statistics;
import com.wineadvisor.wineadvisor.model.wines.fields.Structure;
import com.wineadvisor.wineadvisor.model.wines.fields.Style;
import com.wineadvisor.wineadvisor.model.wines.fields.Taste;
import com.wineadvisor.wineadvisor.model.wines.fields.Vintage;
import com.wineadvisor.wineadvisor.model.wines.fields.WineryEmbedded;
import com.wineadvisor.wineadvisor.repository.ReviewRepository;
import com.wineadvisor.wineadvisor.repository.UserRepository;
import com.wineadvisor.wineadvisor.repository.WineRepository;
import com.wineadvisor.wineadvisor.repository.WineryRepository;
import com.wineadvisor.wineadvisor.repository.CountryRepository;
import com.wineadvisor.wineadvisor.DTO.wines.CreateWineDTO;
import com.wineadvisor.wineadvisor.DTO.wines.NewFoodDTO;
import com.wineadvisor.wineadvisor.DTO.wines.NewGrapeDTO;
import com.wineadvisor.wineadvisor.DTO.wines.NewVintageDTO;
import com.wineadvisor.wineadvisor.DTO.wines.UpdateVintageDTO;
import com.wineadvisor.wineadvisor.DTO.wines.UpdateWineDTO;
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
    ///// Aggregation pipelines /////
    

    //// END of aggr. pipelines /////
    /////////////////////////////////
    

    /////////////////////////////////
    /////// Async. operations ///////

    // Aggiorna una volta al giorno l'oggetto statistics di ogni vintage nella collection wines
    @Scheduled(cron = "0 0 0 * * ?")    // Ogni giorno a mezzanotte
    private void updateStatisticsPerVintage() {
        List<Document> pipeline = Arrays.asList(new Document("$unwind", "$vintages"), 
            new Document("$lookup", 
            new Document("from", "reviews")
                    .append("let", 
            new Document("wineId", "$_id")
                        .append("vintageYear", "$vintages.year"))
                    .append("pipeline", Arrays.asList(new Document("$match", 
                        new Document("$expr", 
                        new Document("$and", Arrays.asList(new Document("$eq", Arrays.asList("$wine_id.id", "$$wineId")), 
                                        new Document("$eq", Arrays.asList("$wine_id.year", "$$vintageYear")))))), 
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
            new Document("$cond", Arrays.asList(new Document("$gt", Arrays.asList(new Document("$size", "$matched_reviews"), 0L)), 
                                new Document("$avg", "$matched_reviews.rating"), 
                                new BsonNull()))))));

        AggregateIterable<Document> results = mongoTemplate
            .getCollection("wines")
            .aggregate(pipeline)
            .allowDiskUse(true);

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
    }

    // Aggiorna una volta al giorno l'oggetto statistics di ogni wine
    @Scheduled(cron = "0 0 0 * * ?")    // Ogni giorno a mezzanotte
    private void updateStatisticsPerWine() {
        List<Document> pipeline = Arrays.asList(new Document("$lookup", 
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
            new Document("$cond", Arrays.asList(new Document("$gt", Arrays.asList(new Document("$size", "$matched_reviews"), 0L)), 
                                new Document("$avg", "$matched_reviews.rating"), 
                                new BsonNull()))))));

        AggregateIterable<Document> results = mongoTemplate
            .getCollection("wines")
            .aggregate(pipeline)
            .allowDiskUse(true);

        for (Document doc : results) {
            Object idObj = doc.get("_id");
            Document stats = (Document) doc.get("statistics");

            // Verifico che tutti i campi siano presenti
            if (idObj == null || stats == null) {
                continue;
            }

            Long wineId = ((Number) idObj).longValue();

            Query query = new Query(Criteria.where("_id").is(wineId));
            Update update = new Update().set("statistics", stats);
            mongoTemplate.updateFirst(query, update, "wines");
        }
    }

    // Aggiorna una volta al giorno il campo wines_count contenuto in grapes, in style, nella collection wines
    @Scheduled(cron = "0 0 0 * * ?")    // Ogni giorno a mezzanotte
    private void updateWinesCountInGrapesPerWine() {
        List<Document> pipeline = Arrays.asList(new Document("$unwind", 
            new Document("path", "$style.grapes")), 
            new Document("$group", 
            new Document("_id", "$style.grapes.name")
                    .append("wines_count",
            new Document("$sum", 1L))));

        AggregateIterable<Document> results = mongoTemplate
            .getCollection("wines")
            .aggregate(pipeline)
            .allowDiskUse(true);

        for (Document doc : results) {
            String grapeName = doc.getString("_id");
            Long winesCount = doc.getLong("wines_count");
        
            if (grapeName == null || winesCount == null) {
                continue;
            }
        
            // Seleziona tutti i vini che contengono questa grape
            Query query = new Query(Criteria.where("style.grapes.name").is(grapeName));
        
            // Aggiorna ogni entry dentro style.grapes con quel nome
            Update update = new Update().set("style.grapes.$[g].wines_count", winesCount);
        
            // Definisce il filtro per gli array ("arrayFilters")
            update.filterArray("g.name", grapeName);
        
            // Applica l'update su tutti i documenti che contengono quella grape
            mongoTemplate.updateMulti(query, update, "wines");
        }            
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
    public Wine addWine(CreateWineDTO createWineDTO, String wineryUsername) {
        // Controllo che esista la winery
        Winery winery = wineryRepository.findByLogin_Username(wineryUsername)
            .orElseThrow(() -> new ResourceNotFoundException("Winery not found with username: " + wineryUsername));
        
        // Controllo che esista il country nella collection country
        Country country = countryRepository.findByName(winery.getCountry())
            .orElseThrow(() -> new ResourceNotFoundException("Country not found with name: " + winery.getCountry()));

        Wine wine = new Wine();
        
        wine.setId(idCounterService.generateSequence("wines"));
        wine.setName(createWineDTO.getName());
        wine.setType(createWineDTO.getType());
        wine.setIsNatural(createWineDTO.getIsNatural());

        wine.setTaste(new Taste());
        wine.getTaste().setStructure(new Structure());
        wine.getTaste().getStructure().setAcidity(createWineDTO.getTaste().getStructure().getAcidity());
        wine.getTaste().getStructure().setFizziness(createWineDTO.getTaste().getStructure().getFizziness());
        wine.getTaste().getStructure().setIntensity(createWineDTO.getTaste().getStructure().getIntensity());
        wine.getTaste().getStructure().setSweetness(createWineDTO.getTaste().getStructure().getSweetness());
        wine.getTaste().getStructure().setTannin(createWineDTO.getTaste().getStructure().getTannin());
        
        wine.setStyle(new Style());
        wine.getStyle().setName(createWineDTO.getStyle().getName());
        wine.getStyle().setDescription(createWineDTO.getStyle().getDescription());
        wine.getStyle().setInterestingFacts(createWineDTO.getStyle().getInterestingFacts());
        wine.getStyle().setBody(createWineDTO.getStyle().getBody());
        wine.getStyle().setAcidity(createWineDTO.getStyle().getAcidity());
        wine.getStyle().setFood(new ArrayList<Food>());
        for (NewFoodDTO food : createWineDTO.getStyle().getFood()){
            Food foodToAdd = new Food();
            foodToAdd.setName(food.getName());
            foodToAdd.setImage(food.getImage());
            wine.getStyle().getFood().add(foodToAdd);
        }
        wine.getStyle().setGrapes(new ArrayList<Grape>());
        for (NewGrapeDTO grape : createWineDTO.getStyle().getGrapes()){
            Grape grapeToAdd = new Grape();
            grapeToAdd.setName(grape.getName());
            grapeToAdd.setWinesCount(0);
            wine.getStyle().getGrapes().add(grapeToAdd);
        }

        wine.getStyle().setBaselineStructure(new BaselineStructure());
        wine.getStyle().getBaselineStructure().setAcidity(createWineDTO.getStyle().getBaselineStructure().getAcidity());
        wine.getStyle().getBaselineStructure().setFizziness(createWineDTO.getStyle().getBaselineStructure().getFizziness());
        wine.getStyle().getBaselineStructure().setIntensity(createWineDTO.getStyle().getBaselineStructure().getIntensity());
        wine.getStyle().getBaselineStructure().setSweetness(createWineDTO.getStyle().getBaselineStructure().getSweetness());
        wine.getStyle().getBaselineStructure().setTannin(createWineDTO.getStyle().getBaselineStructure().getTannin());

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

        return wineRepository.save(wine);
    }


    /// READ operations ///
    // Restituisce un vino per id
    public Wine getWineById(Long wineId) {
        Wine wine = wineRepository.findById(wineId)
            .orElseThrow(() -> new ResourceNotFoundException("Wine with id " + wineId + " not found."));
        return wine;
    }

    // Restituisce un'annata di un vino specifico
    public Vintage getVintage(Long wineId, Integer year) {
        Wine wine = wineRepository.findByIdAndVintages_Year(wineId, year)
            .orElseThrow(() -> new ResourceNotFoundException("Vintage with wineId " + wineId + " and year " + year + " not found."));
        
        for (Vintage vintage : wine.getVintages()) {
            if (vintage.getYear().equals(year)) return vintage;
        }
        return null;
    }

    // Restituisce tutti i vini (con paginazione)
    public Page<Wine> getAllWines(Integer page) {
        Page<Wine> wines = wineRepository.findAll(PageRequest.of(page, PAGE_SIZE));
        checkReturnedPage(wines, "No wines found.");
        return wines;
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
    // Aggiunge una nuova annata ad un determinato vino
    public Wine addVintage(NewVintageDTO newVintage, String username) {
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

                vintage.setCreatedAt(LocalDateTime.now());

                wine.getVintages().add(vintage);

                return wineRepository.save(wine);
            }).orElseThrow(() -> new ResourceNotFoundException("Wine with id " + newVintage.getWineId() + " not found."));
    }

    // Modifica dati del vino 
    public Wine updateWine(UpdateWineDTO updatedWine, String username) {
        return wineRepository.findById(updatedWine.getWineId())
            .map(wine -> {  
                // Controllo che il vino sia della winery username
                if(wineRepository.findByIdAndWinery_Username(updatedWine.getWineId(), username).isEmpty()){
                    throw new ResourceNotFoundException("Winery " + username + " does not own wine with id " + updatedWine.getWineId() + ".");
                }
                
                wine.setName(updatedWine.getName());
                wine.setType(updatedWine.getType());
                wine.setIsNatural(updatedWine.getIsNatural());

                wine.getTaste().getStructure().setAcidity(updatedWine.getTaste().getStructure().getAcidity());
                wine.getTaste().getStructure().setFizziness(updatedWine.getTaste().getStructure().getFizziness());
                wine.getTaste().getStructure().setIntensity(updatedWine.getTaste().getStructure().getIntensity());
                wine.getTaste().getStructure().setSweetness(updatedWine.getTaste().getStructure().getSweetness());
                wine.getTaste().getStructure().setTannin(updatedWine.getTaste().getStructure().getTannin());

                wine.getStyle().setName(updatedWine.getStyle().getName());
                wine.getStyle().setDescription(updatedWine.getStyle().getDescription());
                wine.getStyle().setInterestingFacts(updatedWine.getStyle().getInterestingFacts());
                wine.getStyle().setBody(updatedWine.getStyle().getBody());
                wine.getStyle().setAcidity(updatedWine.getStyle().getAcidity());
                
                for (NewFoodDTO food : updatedWine.getStyle().getFood()){
                    // Controllo che il cibo non esista già
                    if (wine.getStyle().getFood().stream().anyMatch(f -> f.getName().equals(food.getName()))){
                        continue;
                    }
                    Food foodToAdd = new Food();
                    foodToAdd.setName(food.getName());
                    foodToAdd.setImage(food.getImage());
                    wine.getStyle().getFood().add(foodToAdd);
                }
                
                for (NewGrapeDTO grape : updatedWine.getStyle().getGrapes()){
                    // Controllo che il vitigno non esista già
                    if (wine.getStyle().getGrapes().stream().anyMatch(g -> g.getName().equals(grape.getName()))){
                        continue;
                    }
                    Grape grapeToAdd = new Grape();
                    grapeToAdd.setName(grape.getName());
                    grapeToAdd.setWinesCount(0);
                    wine.getStyle().getGrapes().add(grapeToAdd);
                }
                wine.getStyle().getBaselineStructure().setAcidity(updatedWine.getStyle().getBaselineStructure().getAcidity());
                wine.getStyle().getBaselineStructure().setFizziness(updatedWine.getStyle().getBaselineStructure().getFizziness());
                wine.getStyle().getBaselineStructure().setIntensity(updatedWine.getStyle().getBaselineStructure().getIntensity());
                wine.getStyle().getBaselineStructure().setSweetness(updatedWine.getStyle().getBaselineStructure().getSweetness());
                wine.getStyle().getBaselineStructure().setTannin(updatedWine.getStyle().getBaselineStructure().getTannin());

                // Devo aggiornare il nome anche nelle reviews embedded nella collection users
                ArrayList<User> users = userRepository.findByReviews_WineId_Id(updatedWine.getWineId());
                if (!users.isEmpty()){
                    for (User user : users){
                        for (ReviewEmbedded review : user.getReviews()){
                            if (review.getWineId().getId().equals(updatedWine.getWineId())){
                                review.getWineId().setName(updatedWine.getName());
                            }
                        }
                        userRepository.save(user);
                    }
                }

                // Devo aggiornare il nome anche nelle reviews della collection reviews
                ArrayList<Review> reviews = reviewRepository.findByWineId_Id(updatedWine.getWineId());
                if (!reviews.isEmpty()){
                    for (Review review : reviews){
                        review.getWineId().setName(updatedWine.getName());
                        reviewRepository.save(review);
                    }
                }

                return wineRepository.save(wine);
            }).orElseThrow(() -> new ResourceNotFoundException("Wine with id " + updatedWine.getWineId() + " not found."));
    }

    // Modifica dati della vintage di un vino
    public Wine updateVintage(UpdateVintageDTO updatedVintage, String username) {
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
            }).orElseThrow(() -> new ResourceNotFoundException("Vintage with wineId " + updatedVintage.getWineId() + " and year " + updatedVintage.getYear() +" not found."));
    }


    /// DELETE operations ///
    // Elimina un vino
    public void deleteWineById(Long wineId, String username) {
        // Controllo che il vino specificato esista
        if (wineRepository.findById(wineId).isEmpty()){
            throw new ResourceNotFoundException("Wine with id " + wineId + " not found.");
        }

        // Controllo che il vino sia della winery username
        if(wineRepository.findByIdAndWinery_Username(wineId, username).isEmpty()){
            throw new ResourceNotFoundException("Winery " + username + " does not own wine with id " + wineId + ".");
        }
        
        // Se elimino un vino, devo eliminare anche tutte le recensioni fatte su quel vino e togliere
        // l'id di quelle recensioni dagli array "likes"/"dislikes" degli users
        ArrayList<Review> reviews_to_delete = reviewRepository.findByWineId_Id(wineId);
        ArrayList<User> users = (ArrayList<User>) userRepository.findAll();
        for (User user : users){
            boolean modified = user.getReviews().removeIf(r -> r.getWineId().getId().equals(wineId));

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

        reviewRepository.deleteAllByWineId_Id(wineId);
        wineRepository.deleteById(wineId);
    }

    // Elimina una vintage di un vino
    public void deleteVintage(Long wineId, Integer year, String username){
        // Controllo che l'annata specificata esista
        Wine wine = wineRepository.findByIdAndVintages_Year(wineId, year)
            .orElseThrow(() -> new ResourceNotFoundException("Vintage with wineId " + wineId + " and year " + year + " not found."));
        
        // Controllo che il vino sia della winery username
        if(wineRepository.findByIdAndWinery_Username(wineId, username).isEmpty()){
            throw new ResourceNotFoundException("Winery " + username + " does not own wine with id " + wineId + ".");
        }

        ArrayList<Review> reviews_to_delete = reviewRepository.findByWineId_IdAndWineId_Year(wineId, year);
        ArrayList<User> users = (ArrayList<User>) userRepository.findAll();
        for (User user : users){
            boolean modified = user.getReviews().removeIf(r -> r.getWineId().getId().equals(wineId) && r.getWineId().getYear().equals(year));

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

        reviewRepository.deleteAllByWineId_IdAndWineId_Year(wineId, year);
        
        wine.getVintages().removeIf(vintage -> vintage.getYear().equals(year));
        wineRepository.save(wine);
    }

    // Elimina tutti i vini
    public void deleteAllWines(){  
        wineRepository.deleteAll();
    }

    //// END of crud operations ////
    ////////////////////////////////
}
