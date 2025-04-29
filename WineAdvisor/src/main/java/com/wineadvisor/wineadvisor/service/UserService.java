package com.wineadvisor.wineadvisor.service;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.wineadvisor.wineadvisor.exception.ResourceAlreadyExistsException;
import com.wineadvisor.wineadvisor.exception.ResourceNotFoundException;
import com.wineadvisor.wineadvisor.DTO.users.UpdateUserDTO;
import com.wineadvisor.wineadvisor.DTO.users.addFavoriteDTO;
import com.wineadvisor.wineadvisor.DTO.utils.PasswordDTO;
import com.wineadvisor.wineadvisor.DTO.users.CreateUserDTO;
import com.wineadvisor.wineadvisor.exception.BadRequestException;
import com.wineadvisor.wineadvisor.repository.ReviewRepository;
import com.wineadvisor.wineadvisor.repository.UserRepository;
import com.wineadvisor.wineadvisor.repository.WineRepository;
import com.wineadvisor.wineadvisor.model.reviews.Review;
import com.wineadvisor.wineadvisor.model.users.User;
import com.wineadvisor.wineadvisor.model.users.fields.WineFavorite;
import com.wineadvisor.wineadvisor.model.utils.ReviewEmbedded;
import com.wineadvisor.wineadvisor.model.wines.fields.Vintage;

@Service
@RequiredArgsConstructor
public class UserService {
    ////////////////////////////////
    /////////// VARIABLES //////////
    ////////////////////////////////
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final WineRepository wineRepository;

    private final PasswordEncoder passwordEncoder = PasswordDTO.passwordEncoder();

    private static final int PAGE_SIZE = 20;



    ////////////////////////////////
    /////// PRIVATE METHODS ////////
    ////////////////////////////////
    
    ////////////////////////////////
    ///// Checking operations //////
    
    // Controlla che la pagina ritornata dalla repo sia valida e che sia consistente rispetto alle opzioni di paginazione richieste dal client.
    private void checkReturnedPage(Page<User> users, String notFoundMessage) throws ResourceNotFoundException, BadRequestException {
        if (users.getTotalElements() == 0) {
            throw new ResourceNotFoundException(notFoundMessage);
        }
        if (users.getPageable().getPageNumber() >= users.getTotalPages()) {
            throw new BadRequestException("Page requested too high.");
        }
    }
    
    /// END of checking operations //
    /////////////////////////////////
    
    
    /////////////////////////////////
    //// Asynchronous operations ////
    
    // Per ogni utente, scorre l'array dei likes e l'array dei dislikes: per ogni reviewId, controlla che la review esista ancora (se NON esiste più, elimina il reviewId dall'array)
    @Scheduled(cron = "0 0 0 * * ?")
    private void cleanLikesAndDislikes() {
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
    }

    /// END of async. operations ///
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
    public User createUser(CreateUserDTO createUserDTO) throws ResourceAlreadyExistsException, BadRequestException {
        if (userRepository.findByLogin_Username(createUserDTO.getUsername()).isPresent()) {
            throw new ResourceAlreadyExistsException("User with username \"" + createUserDTO.getUsername() + "\" already exists.");
        }
        if (userRepository.findByEmail(createUserDTO.getEmail()).isPresent()) {
            throw new ResourceAlreadyExistsException("User with email \"" + createUserDTO.getEmail() + "\" already exists.");
        }
        if (!createUserDTO.getPasswordDTO().passwordPatternVerifier()) {
            throw new BadRequestException("Password does not meet the minimum requirements: at least 8 characters, 1 digit, 1 lowercase, 1 uppercase, 1 special character among \"!@#$%^&*()\\-_=+.,:;");
        }
        if (!createUserDTO.getPasswordDTO().getNewPass().equals(createUserDTO.getPasswordDTO().getConfirmPass())) {
            throw new BadRequestException("Passwords do not match.");
        }
        
        User newUser = createUserDTO.toUser();
        newUser.adjustFieldsForCreation(passwordEncoder.encode(createUserDTO.getPasswordDTO().getNewPass()));

        return userRepository.save(newUser);
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
        Page<User> users = userRepository.findByName_FirstAndName_Last(firstName, lastName, PageRequest.of(page, PAGE_SIZE));
        checkReturnedPage(users, "Users with first name \"" + firstName + "\" and last name \"" + lastName + "\" not found.");
        return users;
    }

    // Restituisce tutti gli utenti con un determinato nome
    public Page<User> getUsersByFirstName(String firstName, Integer page) throws ResourceNotFoundException, BadRequestException {
        Page<User> users = userRepository.findByName_First(firstName, PageRequest.of(page, PAGE_SIZE));
        checkReturnedPage(users, "Users with first name \"" + firstName + "\" not found.");
        return users;
    }

    // Restituisce tutti gli utenti con un determinato cognome
    public Page<User> getUsersByLastName(String lastName, Integer page) throws ResourceNotFoundException, BadRequestException {
        Page<User> users = userRepository.findByName_Last(lastName, PageRequest.of(page, PAGE_SIZE));
        checkReturnedPage(users, "Users with last name \"" + lastName + "\" not found.");
        return users;
    }

    // Restituisce un utente con un determinato username
    public User getUserByUsername(String username) throws ResourceNotFoundException {
        User user = userRepository.findByLogin_Username(username).orElse(null);
        
        if (user == null) {
            throw new ResourceNotFoundException("User with username \"" + username + "\" not found.");
        }

        return user;
    }

    /// UPDATE operations ///
    // Cerca il documento di un utente con un determinato username e aggiorna l'intero documento con il nuovo passato come argomento
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

                    return userRepository.save(targetUser);
                }
            )
            .orElseThrow(
                () -> new ResourceNotFoundException("User with username \"" + targetUsername + "\" not updatable because it does not exist.")
            );
    }

    // Cerca il documento di un utente e ne modifica lo username
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

                    return userRepository.save(targetUser);
                }
            )
            .orElseThrow(
                () -> new ResourceNotFoundException("Username not updatable because \"" + targetUsername + "\" no user uses it.")
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
                    throw new ResourceAlreadyExistsException(
                            "User with username " + username + " has not liked this review.");
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
                    throw new ResourceAlreadyExistsException(
                            "User with username " + username + " has already disliked this review.");
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
                    throw new ResourceAlreadyExistsException(
                            "User with username " + username + " has not disliked this review.");
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
    // Elimina un utente con un determinato username
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
    }

    //// END of crud operations ////
    ////////////////////////////////
}
