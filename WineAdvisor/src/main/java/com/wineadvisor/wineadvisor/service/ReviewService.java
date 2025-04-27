package com.wineadvisor.wineadvisor.service;

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

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.*;

import com.wineadvisor.wineadvisor.DTO.reviews.CreateReviewDTO;
import com.wineadvisor.wineadvisor.DTO.reviews.UpdateReviewDTO;
import com.wineadvisor.wineadvisor.exception.ResourceAlreadyExistsException;
import com.wineadvisor.wineadvisor.exception.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final WineRepository wineRepository;
    private final UserRepository userRepository;

    @Autowired
    private IdCounterService idCounterService;

    // CRUD operations

    // CREATE
    // Aggiunge una recensione alla collection "reviews" del database
    public Review addReview(CreateReviewDTO createdReview) {
        // Controllo se l'utente esiste
        User user = userRepository.findByLogin_Username(createdReview.getUsername())
            .orElseThrow(() -> new ResourceNotFoundException("User with username " + createdReview.getUsername() + " not found."));

        // Controllo se l'utente ha già recensito il vino
        if (reviewRepository.findByUserId_UsernameAndWineId_IdAndWineId_Year(createdReview.getUsername(), createdReview.getWineId(), createdReview.getYear()).isPresent()) {
            throw new ResourceAlreadyExistsException("User with username " + createdReview.getUsername() + " has already reviewed the wine with id " + createdReview.getWineId() + " and year " + createdReview.getYear() + ".");
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
        Wine wine = wineRepository.findByIdAndVintages_Year(review.getWineId().getId(), review.getWineId().getYear())
            .orElseThrow(() -> new ResourceNotFoundException("Wine with id " + review.getWineId().getId() + " and year " + review.getWineId().getYear() + " not found."));
        
        review.getWineId().setImage(wine.getVintages().get(0).getImage());
        review.getWineId().setName(wine.getName());
        review.setLikesCount((long) 0);
        review.setDislikesCount((long) 0);
        review.setCreatedAt(LocalDateTime.now());
        
        // Setto l'id della recensione
        review.setId(idCounterService.generateSequence("reviews"));

        ReviewEmbedded reviewEmbedded = new ReviewEmbedded(review.getId(), review.getUserId(), review.getWineId(), review.getRating(), review.getText(), review.getCreatedAt(), review.getLikesCount(), review.getDislikesCount());

        // Una volta che la nuova review è stata creata, devo aggiungerla alla lista delle recensioni (della vintage) del vino in wines eliminando
        // la recensione più vecchia se il numero di recensioni supera 3
        for (int i = 0; i < wine.getVintages().size(); i++) {
            if (wine.getVintages().get(i).getYear().equals(review.getWineId().getYear())) {
                if (wine.getVintages().get(i).getReviews().size() >= 3) {
                    wine.getVintages().get(i).getReviews().remove(2);
                }
                wine.getVintages().get(i).getReviews().add(0, reviewEmbedded);
                wineRepository.save(wine);
                break;
            }
        }

        // Aggiungo la recensione alla lista delle recensioni dell'utente (collection users), per cui vale lo stesso discorso fatto per le reviews nei wines
        if(user.getReviews().size() >= 3) {
            user.getReviews().remove(2);
        }
        user.getReviews().add(0, reviewEmbedded);
        userRepository.save(user);

        return reviewRepository.save(review);
    }

    // UPDATE
    // Aggiorna una recensione nella collection "reviews" del db
    public Review updateReview(Long id, UpdateReviewDTO updatedReview) {
        return reviewRepository.findByIdAndUserId_Username(id, updatedReview.getUsername())
                .map(review -> {
                    if(review.getText().equals(updatedReview.getText()) && review.getRating().equals(updatedReview.getRating())) { // Controllo che rating e testo siano stati modificati
                        throw new IllegalArgumentException("Rating and text are the same as the previous one.");
                    }
                    review.setRating(updatedReview.getRating());
                    review.setText(updatedReview.getText());

                    Wine wine = wineRepository.findByIdAndVintages_Year(review.getWineId().getId(), review.getWineId().getYear())
                            .orElseThrow(() -> new ResourceNotFoundException("Wine with id " + review.getWineId().getId() + " and year " + review.getWineId().getYear() + " not found."));
                    
                    User user = userRepository.findByLogin_Username(review.getUserId().getUsername())
                            .orElseThrow(() -> new ResourceNotFoundException("User with username " + review.getUserId().getUsername() + " not found."));

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

                    return reviewRepository.save(review);
                }).orElseThrow(() -> new ResourceNotFoundException("Review with id " + id +  " and with username " + updatedReview.getUsername() + " not found."));
    }

    // READ
    // Cerca una recensione per id nella collection "reviews" del database
    public Optional<Review> getReviewById(Long id) {
        Optional<Review> review = reviewRepository.findById(id);
        if (review.isEmpty()) {
            throw new ResourceNotFoundException("Review with id " + id + " not found.");
        }
        return review;
    }

    // Restituisce tutte le recensioni dalla collection "reviews" del database
    public Page<Review> getAllReviews(Pageable pageable) {
        return reviewRepository.findAll(pageable);
    }    

    // Restituisce tutte le recensioni di un vino specifico di un'annata specifica dalla collection "reviews" del database
    public Page<Review> getReviewsByVintage(Pageable pageable, Long wineId, Integer vintageYear) {
        Page<Review> reviews = reviewRepository.findByWineId_IdAndWineId_Year(pageable, wineId, vintageYear);
        if (reviews.isEmpty()) {
            throw new ResourceNotFoundException("Reviews for wine with id " + wineId + " and year " + vintageYear + " not found.");
        }
        return reviews;
    }

    // Restituisce tutte le recensioni di un vino specifico dalla collection "reviews" del database
    public Page<Review> getReviewsByWine(Pageable pageable, Long wineId) {
        Page<Review> reviews = reviewRepository.findByWineId_Id(pageable, wineId);
        if (reviews.isEmpty()) {
            throw new ResourceNotFoundException("Reviews for wine with id " + wineId + " not found.");
        }
        return reviews;
    }

    // Restituisce tutte le recensioni di un utente specifico dalla collection "reviews" del database
    public Page<Review> getReviewsByUser(Pageable pageable, String username) {
        Page<Review> reviews = reviewRepository.findByUserId_Username(pageable, username);
        if (reviews.isEmpty()) {
            throw new ResourceNotFoundException("Reviews for user with username " + username + " not found.");
        }
        return reviews;
    }

    // Restituisce tutte le recensioni di un utente specifico per un vino specifico dalla collection "reviews" del database
    public Page<Review> getReviewsByUserAndWine(Pageable pageable, String username, Long wineId) {
        Page<Review> reviews = reviewRepository.findByUserId_UsernameAndWineId_Id(pageable, username, wineId);
        if (reviews.isEmpty()) {
            throw new ResourceNotFoundException("Reviews for user with username " + username + " and wine with id " + wineId + " not found.");
        }
        return reviews;
    }

    // Restituisce il numero di recensioni totali presenti nella collection
    public Long getReviewsCount() {
        return reviewRepository.count();
    }

    // Restituisce il numero di recensioni fatte per un determinato vino
    public Long getReviewsCountByWine(Long wineId) {
        // Controllo se il vino esiste
        if (wineRepository.findById(wineId).isEmpty()) {
            throw new ResourceNotFoundException("Wine with id " + wineId + " not found.");
        }
        return reviewRepository.countByWineId_Id(wineId);
    }

    // Restituisce il numero di recensioni di un determinato utente
    public Long getReviewsCountByUser(String username) {
        // Controllo se l'utente esiste
        if (userRepository.findByLogin_Username(username).isEmpty()) {
            throw new ResourceNotFoundException("User with username " + username + " not found.");
        }
        return reviewRepository.countByUserId_Username(username);
    }

    // Restituisce il numero di recensioni di una determinata annata di un vino
    public Long getReviewsCountByVintage(Long wineId, Integer vintageYear) {
        // Controllo se il vino esiste e se l'annata esiste
        if (wineRepository.findById(wineId).isEmpty()) {
            throw new ResourceNotFoundException("Wine with id " + wineId + " not found.");
        }
        if (wineRepository.findByIdAndVintages_Year(wineId, vintageYear).isEmpty()) {
            throw new ResourceNotFoundException("Wine with id " + wineId + " and year " + vintageYear + " not found.");
        }

        return reviewRepository.countByWineId_IdAndWineId_Year(wineId, vintageYear);
    }

    // Calcola e restituisce la media dei rating di un'annata di un vino
    public Double getAverageRatingByVintage(Long wineId, Integer year){
        // Controllo se il vino e la vintage esistono
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
    public Page<Review> getReviewsByWineAndRatingRange(Pageable pageable, Long wineId, Double minRating, Double maxRating) {
        // Controllo se il vino esiste
        if (wineRepository.findById(wineId).isEmpty()) {
            throw new ResourceNotFoundException("Wine with id " + wineId + " not found.");
        }
        Page<Review> reviews =  reviewRepository.findByWineId_IdAndRatingBetween(pageable, wineId, minRating, maxRating);

        // Controllo se reviews è vuoto
        if (reviews.isEmpty()) {
            throw new ResourceNotFoundException("No reviews found for wine with id " + wineId + " in the rating range [" + minRating + ", " + maxRating + "].");
        }
        return reviews;
    }

    // Restituisce le recensioni di un'annata specifica per un determinato vino in un range di rating specifico
    public Page<Review> getReviewsByVintageAndRatingRange(Pageable pageable, Long wineId, Integer year, Double minRating, Double maxRating) {
        // Controllo se il vino e la vintage esistono
        if (wineRepository.findById(wineId).isEmpty()) {
            throw new ResourceNotFoundException("Wine with id " + wineId + " not found.");
        }
        if (wineRepository.findByIdAndVintages_Year(wineId, year) == null) {
            throw new ResourceNotFoundException("Wine with id " + wineId + " and year " + year + " not found.");
        }

        Page<Review> reviews = reviewRepository.findByWineId_IdAndWineId_YearAndRatingBetween(pageable, wineId, year, minRating, maxRating);
        // Controllo se reviews è vuoto
        if (reviews.isEmpty()) {
            throw new ResourceNotFoundException("No reviews found for wine with id " + wineId + " and year " + year + " in the rating range [" + minRating + ", " + maxRating + "].");
        }
        return reviews;
    }

    // Restituisce le num recensioni più popolari (con più like) di un'annata specifica per un determinato vino
    public ArrayList<Review> getPopularReviewsByVintage(Long wineId, Integer vintageYear, int num) {
        // Controllo se il vino e la vintage esistono
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

    // DELETE
    // Cancella una recensione specifica
    public void deleteReviewById(Long id) {
        // Controllo se la recensione esiste
        reviewRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Review with id " + id + " not found."));

        // Rimuovo la recensione dal vino (se presente)
        Wine wine = wineRepository.findByVintages_Reviews_ReviewId(id)
            .orElseThrow(() -> new ResourceNotFoundException("Wine with review id " + id + " not found."));
        for (Vintage vintage : wine.getVintages()) {
            if (vintage.getReviews().removeIf(r -> r.getReviewId().equals(id))) {
                wineRepository.save(wine);
                break;
            }
        }

        // Rimuovo la recensione dall'utente (se presente)
        User user_to_find = userRepository.findByReviews_ReviewId(id)
            .orElseThrow(() -> new ResourceNotFoundException("User with review id " + id + " not found."));
        for (ReviewEmbedded review : user_to_find.getReviews()) {
            if (review.getReviewId().equals(id)) {
                user_to_find.getReviews().remove(review);
                userRepository.save(user_to_find);
                break;
            }
        }

        // Rimuovo l'id della recensione dagli array likes/dislikes di users
        ArrayList<User> users = userRepository.findByLikesDislikes(id);
        for (User user : users) {
            user.getLikes().removeIf(l -> l.equals(id));
            user.getDislikes().removeIf(d -> d.equals(id));
            userRepository.save(user);
        }

        reviewRepository.deleteById(id);
    }

    // Cancella tutte le recensioni di un vino specifico
    public void deleteReviewsByWine(Long wineId) {
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

        // 

        reviewRepository.deleteAllByWineId_Id(wineId);
    }

    // Cancella tutte le recensioni di un utente specifico
    public void deleteReviewsByUser(String username) {
        // Controllo se l'utente esiste
        User user = userRepository.findByLogin_Username(username)
            .orElseThrow(() -> new ResourceNotFoundException("User with username " + username + " not found."));

        // Controllo se esistono recensioni fatte da quell'utente
        if (reviewRepository.findByUserId_Username(username).isEmpty()){
            throw new ResourceNotFoundException("Reviews with username " + username + " not found.");
        }

        // Rimuovo gli id delle recensioni dagli array likes/dislikes di users
        ArrayList<ReviewEmbedded> reviews = user.getReviews();
        for (ReviewEmbedded review : reviews) {
            ArrayList<User> users = userRepository.findByLikesDislikes(review.getReviewId());
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
    }

    // Cancella tutte le recensioni di un'annata specifica di un vino specifico
    public void deleteReviewsByVintage(Long wineId, Integer vintageYear) {
        // Controllo se il vino e la vintage esistono
        Wine wine = wineRepository.findById(wineId)
            .orElseThrow(() -> new ResourceNotFoundException("Wine with id " + wineId + " not found."));

        if (wineRepository.findByIdAndVintages_Year(wineId, vintageYear).isEmpty()) {
            throw new ResourceNotFoundException("Wine with id " + wineId + " and year " + vintageYear + " not found.");
        }

        // Rimuovo le recensioni dalla vintage nella collection wines
        for (Vintage vintage : wine.getVintages()) {
            if (vintage.getYear().equals(vintageYear)) {
                vintage.getReviews().clear();
                break;
            }
        }
        wineRepository.save(wine);

        // Devo rimuoverle anche dall collection users
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
    }

    // Cancella tutte le recensioni
    public void deleteAllReviews() {
        // Rimuovo le recensioni da tutti i vini
        List<Wine> wines = wineRepository.findAll();
        for (Wine wine : wines) {
            for (Vintage vintage : wine.getVintages()) {
                vintage.getReviews().clear();
            }
            wineRepository.save(wine);
        }

        // Rimuovo le recensioni da tutti gli utenti
        List<User> users = userRepository.findAll();
        for (User user : users) {
            user.getReviews().clear();
            user.getLikes().clear();
            user.getDislikes().clear();
            userRepository.save(user);
        }

        // Elimino tutte le recensioni dalla collection principale
        reviewRepository.deleteAll();
    }


    // Funzioni di utilità
    // Restituisce le num recensioni più recenti
    public ArrayList<Review> getRecentReviews(int num) {
        ArrayList<Review> reviews = (ArrayList<Review>) reviewRepository.findAll();
        // Controllo se reviews è vuoto
        if (reviews.isEmpty()) {
            throw new ResourceNotFoundException("No reviews found.");
        }

        // Ordino le recensioni in base alla data di creazione (createdAt) in ordine decrescente
        ArrayList<Review> sortedReviews = sortReviewsByField(reviews, "createdAt", false);

        // Prendo solo il numero richiesto di recensioni, evitando errori di indice
        int limit = Math.min(num, sortedReviews.size());
        return new ArrayList<>(sortedReviews.subList(0, limit));
    }

    // Restituisce le num recensioni più recenti di un utente specifico
    public ArrayList<Review> getRecentReviewsByUser(String username, int num) {
        // Controllo se l'utente esiste
        if (userRepository.findByLogin_Username(username).isEmpty()) {
            throw new ResourceNotFoundException("User with username " + username + " not found.");
        }
        
        ArrayList<Review> reviews = reviewRepository.findByUserId_Username(username);
        // Controllo se reviews è vuoto
        if (reviews.isEmpty()) {
            throw new ResourceNotFoundException("No reviews found for user with username " + username + ".");
        }

        // Ordino le recensioni in base alla data di creazione (createdAt) in ordine decrescente
        ArrayList<Review> sortedReviews = sortReviewsByField(reviews, "createdAt", false);
        
        // Prendo solo il numero richiesto di recensioni, evitando errori di indice
        int limit = Math.min(num, sortedReviews.size());
        return new ArrayList<>(sortedReviews.subList(0, limit));
    }

    // Restituisce le num recensioni più recenti di un'annata specifica per un determinato vino
    public ArrayList<Review> getRecentReviewsByVintage(Long wineId, Integer vintageYear, int num) {
        // Controllo se il vino e la vintage esistono
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

        // Ordino le recensioni in base alla data di creazione (createdAt) in ordine decrescente
        ArrayList<Review> recentReviews = sortReviewsByField(reviews, "createdAt", false);
        
        // Prendo solo il numero richiesto di recensioni, evitando errori di indice
        int limit = Math.min(num, recentReviews.size()); 
        return new ArrayList<>(recentReviews.subList(0, limit));
    }

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

    // Da chiamare dopo che è stata rimossa una (sola) recensione, aggiorna le collection wines, in cui per ogni vintage
    // devono esserci le 3 recensioni più recenti
    public void updateWinesReviews(Long wineId, Integer vintageYear) {
        Optional<Wine> wine_to_find = wineRepository.findByIdAndVintages_Year(wineId, vintageYear);
        if(wine_to_find.isPresent()){
            Wine wine = wine_to_find.get();

            // prendo le reviews di quell'annata di quel vino dalla collection delle reviews
            ArrayList<Review> reviews = reviewRepository.findByWineId_IdAndWineId_Year(wineId, vintageYear);

            // ordino le reviews in base alla data di creazione (createdAt) in ordine decrescente e prendo solo le prime 3
            ArrayList<Review> reviews_sorted = sortReviewsByField(reviews, "created_at", false);
            int limit = Math.min(3, reviews_sorted.size());
            ArrayList<Review> recentReviews = new ArrayList<>(reviews_sorted.subList(0, limit));
            
            // Devo farle diventare del tipo Reviewembedded
            ArrayList<ReviewEmbedded> recentReviewsEmbedded = new ArrayList<>();
            for (int i = 0; i < recentReviews.size(); i++){
                ReviewEmbedded reviewEmbedded = new ReviewEmbedded(recentReviews.get(i).getId(), recentReviews.get(i).getUserId(), recentReviews.get(i).getWineId(), recentReviews.get(i).getRating(), recentReviews.get(i).getText(), recentReviews.get(i).getCreatedAt(), recentReviews.get(i).getLikesCount(), recentReviews.get(i).getDislikesCount());
                recentReviewsEmbedded.add(reviewEmbedded);
            }

            // aggiorno la vintage di quel vino con le (max 3) recensioni più recenti
            for (int i = 0; i < wine.getVintages().size(); i++){
                if (wine.getVintages().get(i).getYear().equals(vintageYear)){
                    wine.getVintages().get(i).setReviews(recentReviewsEmbedded);
                    break;
                }
            }
            wineRepository.save(wine);
        }
    }

    // Da chiamare dopo che è stata rimossa una (sola) recensione, aggiorna la collection users dalla repository, in cui per ogni utente devono esserci le 3 recensioni più recenti
    public void updateUsersReviews(String username){
        Optional<User> user_to_find = userRepository.findByLogin_Username(username);
        if(user_to_find.isPresent()){
            User user = user_to_find.get();
            // prendo le reviews di quell'utente dalla collection delle reviews
            ArrayList<Review> reviews = reviewRepository.findByUserId_Username(username);
            // ordino le reviews in base alla data di creazione (createdAt) in ordine decrescente e prendo solo le prime 3
            ArrayList<Review> reviews_sorted = sortReviewsByField(reviews, "created_at", false);
            int limit = Math.min(3, reviews_sorted.size());
            ArrayList<Review> recentReviews = new ArrayList<>(reviews_sorted.subList(0, limit));

            // Devo farle diventare del tipo ReviewEmbedded
            ArrayList<ReviewEmbedded> recentReviewsEmbedded = new ArrayList<>();
            for (int i = 0; i < recentReviews.size(); i++){
                ReviewEmbedded reviewEmbedded = new ReviewEmbedded(recentReviews.get(i).getId(), recentReviews.get(i).getUserId(), recentReviews.get(i).getWineId(), recentReviews.get(i).getRating(), recentReviews.get(i).getText(), recentReviews.get(i).getCreatedAt(), recentReviews.get(i).getLikesCount(), recentReviews.get(i).getDislikesCount());
                recentReviewsEmbedded.add(reviewEmbedded);
            }

            // aggiorno l'utente con le (max 3) recensioni più recenti
            user.setReviews(recentReviewsEmbedded);
            userRepository.save(user);
        }
    }

    // OPERAZIONI ASINCRONE
    // Operazione che una volta al giorno aggiorna le recensioni più recenti di ogni vino e di ogni utente (3 al massimo)
    @Scheduled(cron = "0 0 0 * * ?") // Ogni giorno a mezzanotte
    public void updateWinesAndUsers(){
        ArrayList<Wine> wines = (ArrayList<Wine>) wineRepository.findAll();
        for (int i = 0; i < wines.size(); i++){
            ArrayList<Vintage> vintages = wines.get(i).getVintages();
            for (int j = 0; j < vintages.size(); j++){
                updateWinesReviews(wines.get(i).getId(), vintages.get(j).getYear());
            }
        }

        ArrayList<User> users = (ArrayList<User>) userRepository.findAll();
        for (int i = 0; i < users.size(); i++){
            updateUsersReviews(users.get(i).getLogin().getUsername());
        }
    }

    // Operazione che una volta al giorno aggiorna i campi: "statistics": {"ratings_count": 199, "ratings_average": 4.3} presenti all'interno di ogni vintage di ogni wine nella collection wines
    @Scheduled(cron = "0 0 0 * * ?") // Ogni giorno a mezzanotte
    public void updateStatistics(){
        ArrayList<Wine> wines = (ArrayList<Wine>) wineRepository.findAll();
        for (int i = 0; i < wines.size(); i++){
            ArrayList<Vintage> vintages = wines.get(i).getVintages();
            for (int j = 0; j < vintages.size(); j++){
                ArrayList<Review> reviews = reviewRepository.findByWineId_IdAndWineId_Year(wines.get(i).getId(), vintages.get(j).getYear());
                if (reviews.isEmpty()){
                    vintages.get(j).getStatistics().setRatingsCount((long) 0);
                    vintages.get(j).getStatistics().setRatingsAverage(0.0);
                } else {
                    Long ratings_count = (long) reviews.size();
                    Double ratings_average = getAverageRatingByVintage(wines.get(i).getId(), vintages.get(j).getYear());
                    vintages.get(j).getStatistics().setRatingsCount(ratings_count);
                    vintages.get(j).getStatistics().setRatingsAverage(ratings_average);
                }
            }
            wineRepository.save(wines.get(i));
        }
    }
}
