package com.wineadvisor.wineadvisor.service;

import java.util.ArrayList;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.wineadvisor.wineadvisor.model.reviews.Review;
import com.wineadvisor.wineadvisor.model.users.User;
import com.wineadvisor.wineadvisor.model.utils.ReviewEmbedded;
import com.wineadvisor.wineadvisor.model.wineries.Winery;
import com.wineadvisor.wineadvisor.model.wines.*;
import com.wineadvisor.wineadvisor.model.wines.fields.BaselineStructure;
import com.wineadvisor.wineadvisor.model.wines.fields.Country;
import com.wineadvisor.wineadvisor.model.wines.fields.Food;
import com.wineadvisor.wineadvisor.model.wines.fields.Grape;
import com.wineadvisor.wineadvisor.model.wines.fields.Region;
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
import com.wineadvisor.wineadvisor.exception.ResourceAlreadyExistsException;
import com.wineadvisor.wineadvisor.exception.ResourceNotFoundException;


@Service
@RequiredArgsConstructor
public class WineService {
    private final ReviewRepository reviewRepository;
    private final WineRepository wineRepository;
    private final UserRepository userRepository;
    private final WineryRepository wineryRepository;
    private final CountryRepository countryRepository;
    private final IdCounterService idCounterService;

    // CRUD
    // CREATE
    // Aggiunge un nuovo vino alla collection wines
    public Wine addWine(CreateWineDTO createWineDTO, Long wineryId) {
        // Controllo che esista la winery
        Winery winery = wineryRepository.findById(wineryId)
            .orElseThrow(() -> new ResourceNotFoundException("Winery not found with id: " + wineryId));
        
        // Controllo che esista il country nella collection wineries
        Country country = countryRepository.findByName(winery.getCountry())
            .orElseThrow(() -> new ResourceNotFoundException("Country not found with name: " + winery.getCountry()));

        Wine wine = new Wine();
        
        wine.setId(idCounterService.generateSequence("wine"));
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
        
        wine.setRegion(new Region());
        wine.getRegion().setName(winery.getRegion());
        wine.getRegion().setCountry(country);

        return wineRepository.save(wine);
    }

    // READ
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
    public Page<Wine> getAllWines(Pageable pageable) {
        return wineRepository.findAll(pageable);
    }

    // Restituisce tutti i vini che nel nome contengono una certa keyword (con paginazione)
    public Page<Wine> getWinesByName(Pageable pageable, String keyword) {
        Page<Wine> wines = wineRepository.findByNameContainingIgnoreCase(pageable, keyword);

        if (wines.isEmpty()){
            throw new ResourceNotFoundException("No wines found with name containing " + keyword + ".");
        }
        return wines;
    }

    
    // Restituisce tutti i vini di una determinata winery (con paginazione)
    public Page<Wine> getWinesByWinery(Pageable pageable, Long wineryId) {
        // Controllo che la winery indicata esista
        wineryRepository.findById(wineryId)
        .orElseThrow(() -> new ResourceNotFoundException("Winery with id " + wineryId + " not found."));
        
        Page<Wine> wines = wineRepository.findByWinery_Id(pageable, wineryId);
        if (wines.isEmpty()){
            throw new ResourceNotFoundException("No wines found for winery with id " + wineryId + ".");
        }
        return wines;
    }
    
    // Restituisce tutti i vini di una determinata regione (con paginazione)
    public Page<Wine> getWinesByRegion(Pageable pageable, String region) {
        Page<Wine> wines = wineRepository.findByRegion_Name(pageable, region);
        if (wines.isEmpty()){
            throw new ResourceNotFoundException("No wines found in region " + region + ".");
        }
        return wines;
    }

    // Restituisce tutti i vini di una determinata nazione (con paginazione)
    public Page<Wine> getWinesByCountry(Pageable pageable, String country) {
        Page<Wine> wines = wineRepository.findByRegion_Country_Name(pageable, country);
        if (wines.isEmpty()){
            throw new ResourceNotFoundException("No wines found in country " + country + ".");
        }
        return wines;
    }

    // Restituisce tutti i vini di una determinata tipologia (con paginazione)
    public Page<Wine> getWinesByType(Pageable pageable, String type) {
        Page<Wine> wines = wineRepository.findByType(pageable, type);
        if (wines.isEmpty()){
            throw new ResourceNotFoundException("No wines found of type " + type + ".");
        }
        return wines;
    }

    // Restituisce tutti i vini di un determinato vitigno (con paginazione)
    public Page<Wine> getWinesByGrapeName(Pageable pageable, String grapeName) {
        Page<Wine> wines = wineRepository.findByStyle_Grapes_Name(pageable, grapeName);

        if (wines.isEmpty()){
            throw new ResourceNotFoundException("No wines found with grape name " + grapeName + ".");
        }
        return wines;
    }

    // Restituisce tutti i vini in cui sono presenti annate con prezzi compresi tra min_price e max_price (con paginazione)
    public Page<Wine> getWinesByPriceRange(Pageable pageable, Double min_price, Double max_price) {
        Page<Wine> wines = wineRepository.findByVintages_PriceBetween(pageable, min_price, max_price);
        if (wines.isEmpty()){
            throw new ResourceNotFoundException("No wines found with price between " + min_price + " and " + max_price + ".");
        }
        return wines;
    }

    // Restituisce tutti i vini con media recensioni superiore o uguale a minRating (con paginazione)
    public Page<Wine> getWinesByMinAverageRating(Pageable pageable, Double minRating) {
        Page<Wine> wines = wineRepository.findByStatistics_RatingsAverageGreaterThanEqual(pageable, minRating);
        if (wines.isEmpty()){
            throw new ResourceNotFoundException("No wines found with average rating greater than or equal to " + minRating + ".");
        }
        return wines;
    }

    // UPDATE
    // Aggiunge una nuova annata ad un determinato vino
    public Wine addVintage(NewVintageDTO newVintage) {
        return wineRepository.findById(newVintage.getWineId())
            .map(wine -> {
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

                wine.getVintages().add(vintage);

                return wineRepository.save(wine);
            }).orElseThrow(() -> new ResourceNotFoundException("Wine with id " + newVintage.getWineId() + " not found."));
    }

    // Modifica dati del vino 
    public Wine updateWine(UpdateWineDTO updatedWine) {
        return wineRepository.findById(updatedWine.getWineId())
            .map(wine -> {                
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
    public Wine updateVintage(UpdateVintageDTO updatedVintage) {
        return wineRepository.findByIdAndVintages_Year(updatedVintage.getWineId(), updatedVintage.getYear())
            .map(wine -> {                
                
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


    // DELETE
    // Elimina un vino
    public void deleteWineById(Long wineId) {
        // Controllo che il vino specificato esista
        if (wineRepository.findById(wineId).isEmpty()){
            throw new ResourceNotFoundException("Wine with id " + wineId + " not found.");
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
    public void deleteVintage(Long wineId, Integer year){
        // Controllo che l'annata specificata esista
        Wine wine = wineRepository.findByIdAndVintages_Year(wineId, year)
            .orElseThrow(() -> new ResourceNotFoundException("Vintage with wineId " + wineId + " and year " + year + " not found."));
        

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
        // Elimina tutte le recensioni associate ai vini
        reviewRepository.deleteAll();

        // Elimino in tutti gli utenti le recensioni
        ArrayList<User> users = (ArrayList<User>) userRepository.findAll();
        for (User user : users){
            user.getReviews().clear();
            user.getLikes().clear();
            user.getDislikes().clear();
            userRepository.save(user);
        }
        
        // Elimina tutti i vini
        wineRepository.deleteAll();
    }
}
