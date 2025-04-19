package com.wineadvisor.wineadvisor.service;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;

import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.wineadvisor.wineadvisor.exception.ResourceAlreadyExistsException;
import com.wineadvisor.wineadvisor.exception.ResourceNotFoundException;
import com.wineadvisor.wineadvisor.DTO.users.PasswordDTO;
import com.wineadvisor.wineadvisor.DTO.users.UpdateUserDTO;
import com.wineadvisor.wineadvisor.DTO.users.CreateUserDTO;
import com.wineadvisor.wineadvisor.exception.BadRequestException;
import com.wineadvisor.wineadvisor.repository.ReviewRepository;
import com.wineadvisor.wineadvisor.repository.UserRepository;
import com.wineadvisor.wineadvisor.repository.WineRepository;
import com.wineadvisor.wineadvisor.model.Review;
import com.wineadvisor.wineadvisor.model.User;
import com.wineadvisor.wineadvisor.model.fields.ReviewEmbedded;
import com.wineadvisor.wineadvisor.model.fields.wines.Vintage;

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



    ////////////////////////////////
    //////// PRIVATE METHODS ///////
    ////////////////////////////////
    
    ////////////////////////////////
    ////// Updates on wines ////////
    
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

    // Ricerca una review nella collection wines (per ogni wine, per ogni vintage ho le reviews) e la elimina correttamente
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

    //// END of updates on wines ////
    /////////////////////////////////


    /////////////////////////////////
    ////// Updates on reviews ///////
    
    // Ricerca una review nella collection reviews e ne aggiorna correttamente la thumbnail dell'utente
    private void updateReview_UserId_ThumbnailByUsername(String targetUsername, String updatedThumbnail) {
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

    // Ricerca una review nella collection reviews e ne aggiorna correttamente lo username dell'utente
    private void updateReview_UserId_UsernameByUsername(String targetUsername, String updatedUsername) {
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

    // Ricerca una review nella collection reviews e la elimina correttamente
    private void deleteReviewByUserId_Username(String targetUsername) {
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

    /// END of updates on reviews ///
    /////////////////////////////////


    
    /////////////////////////////////
    //////// PUBLIC METHODS /////////
    /////////////////////////////////

    /////////////////////////////////
    /////// CRUD operations /////////

    /// CREATE operations ///
    // Aggiunge un utente alla collection "users" del database
    public User createUser(CreateUserDTO createUserDTO) throws ResourceAlreadyExistsException, BadRequestException {
        // System.out.println("createUserDTO: " + createUserDTO.toString());
        // if (true) {
        //     throw new DebugException();
        // }

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
    public Page<User> getAllUsers(Pageable pageable) throws ResourceNotFoundException {
        Page<User> users = userRepository.findAll(pageable);

        if (users.getTotalElements() == 0) {
            throw new ResourceNotFoundException("No users found.");
        }
        if (users.getPageable().getPageNumber() > users.getTotalPages()) {
            throw new BadRequestException("Page requested too high.");
        }

        return users;
    }    

    // Restituisce tutti gli utenti con un determinato nome e cognome
    public Page<User> getUsersByFullName(String firstName, String lastName, Pageable pageable) throws ResourceNotFoundException {
        // ArrayList<User> result = userRepository.findByName_Last(lastName);
        // result.removeIf(user -> !user.getName().getFirst().equals(firstName));

        // if (result.isEmpty()) {
        //     throw new ResourceNotFoundException("Users with first name \"" + firstName + "\" and last name \"" + lastName + "\" not found.");
        // }

        // return result;
        Page<User> users = userRepository.findByName_FirstAndName_Last(firstName, lastName, pageable);

        if (users.getTotalElements() == 0) {
            throw new ResourceNotFoundException("Users with first name \"" + firstName + "\" and last name \"" + lastName + "\" not found.");
        }
        if (users.getPageable().getPageNumber() > users.getTotalPages()) {
            throw new BadRequestException("Page requested too high.");
        }

        return users;
    }

    // Restituisce tutti gli utenti con un determinato nome
    public Page<User> getUsersByFirstName(String firstName, Pageable pageable) throws ResourceNotFoundException {
        // ArrayList<User> result = userRepository.findByName_First(firstName);
        
        // if (result.isEmpty()) {
        //     throw new ResourceNotFoundException("Users with first name \"" + firstName + "\" not found.");
        // }

        // return result;
        Page<User> users = userRepository.findByName_First(firstName, pageable);

        if (users.getTotalElements() == 0) {
            throw new ResourceNotFoundException("Users with first name \"" + firstName + "\" not found.");
        }
        if (users.getPageable().getPageNumber() > users.getTotalPages()) {
            throw new BadRequestException("Page requested too high.");
        }

        return users;
    }

    // Restituisce tutti gli utenti con un determinato cognome
    public Page<User> getUsersByLastName(String lastName, Pageable pageable) throws ResourceNotFoundException {
        // ArrayList<User> result = userRepository.findByName_Last(lastName);
        
        // if (result.isEmpty()) {
        //     throw new ResourceNotFoundException("Users with last name \"" + lastName + "\" not found.");
        // }

        // return result;
        Page<User> users = userRepository.findByName_Last(lastName, pageable);

        if (users.getTotalElements() == 0) {
            throw new ResourceNotFoundException("Users with last name \"" + lastName + "\" not found.");
        }
        if (users.getPageable().getPageNumber() > users.getTotalPages()) {
            throw new BadRequestException("Page requested too high.");
        }

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
        final User user = userRepository
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
                        updateReview_UserId_ThumbnailByUsername(targetUser.getLogin().getUsername(), targetUser.getPicture().getThumbnail());
                    }

                    // Finalizzo gli aggiornamenti in modo da evitare incosistenze nel database
                    targetUser.adjustFieldsForUpdate();

                    return userRepository.save(targetUser);
                }
            )
            .orElseThrow(
                () -> new ResourceNotFoundException("User with username \"" + targetUsername + "\" not updatable because it does not exist.")
            );

        return user;
    }

    // Cerca il documento di un utente e ne modifica lo username
    public User updateUserUsername(String targetUsername, String newUsername) throws ResourceNotFoundException, ResourceAlreadyExistsException, BadRequestException{
        if (targetUsername.equals(newUsername)) {
            throw new BadRequestException("Username not updatable because it is the same as the old one.");
        }
        if (userRepository.findByLogin_Username(newUsername).isPresent()) {
            throw new ResourceAlreadyExistsException("Username not updatable because \"" + newUsername + "\" is already used by another user.");
        }

        final User user = userRepository
            .findByLogin_Username(targetUsername)
            .map(
                targetUser -> {
                    targetUser.getLogin().setUsername(newUsername.trim());
                    if (targetUser.getReviews().size() > 0) {
                        for (ReviewEmbedded review : targetUser.getReviews()) {
                            review.getUserId().setUsername(newUsername);
                        }

                        // Aggiorno tutte le review dell'utente nella collection "reviews"
                        updateReview_UserId_UsernameByUsername(targetUsername, newUsername);
                    }

                    return userRepository.save(targetUser);
                }
            )
            .orElseThrow(
                () -> new ResourceNotFoundException("Username not updatable because \"" + targetUsername + "\" no user uses it.")
            );
        
        return user;
    }

    // Cerca il documento di un utente e ne modifica la password
    public User updateUserPassword(String username, PasswordDTO passwordDTO) throws IllegalArgumentException, ResourceNotFoundException {
        passwordDTO.setOldPass(passwordDTO.getOldPass().trim());
        passwordDTO.setNewPass(passwordDTO.getNewPass().trim());
        passwordDTO.setConfirmPass(passwordDTO.getConfirmPass().trim());

        return userRepository
            .findByLogin_Username(username)
            .map(
                targetUser -> {
                    if (!passwordEncoder.matches(passwordDTO.getOldPass(), targetUser.getLogin().getPassword())) {
                        throw new IllegalArgumentException("Password of user with username \"" + username + "\" not updatable because old password is wrong.");
                    }
                    if (passwordDTO.getNewPass().equals(passwordDTO.getOldPass())) {
                        throw new IllegalArgumentException("Password of user with username \"" + username + "\" not updatable because new password is the same as the old one.");
                    }
                    if (!passwordDTO.getNewPass().equals(passwordDTO.getConfirmPass())) {
                        throw new IllegalArgumentException("Password of user with username \"" + username + "\" not updatable because new passwords do not match.");
                    }
                    if (!passwordDTO.passwordPatternVerifier()) {
                        throw new IllegalArgumentException("Password does not meet the minimum requirements: at least 8 characters, 1 digit, 1 lowercase, 1 uppercase, 1 special character among \"!@#$%^&*()\\-_=+.,:;");
                    }

                    targetUser.getLogin().setPassword(passwordEncoder.encode(passwordDTO.getNewPass()));
                    return userRepository.save(targetUser);
                }
            )
            .orElseThrow(
                () -> new ResourceNotFoundException("User with username \"" + username + "\" not updatable because it does not exist.")
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
                    throw new ResourceAlreadyExistsException("User with username " + username + " has not liked this review.");
                }

                // Rimuovo l'utente dalla lista di chi ha messo like alla recensione e decremento il numero di likes della recensione
                user.getLikes().remove(reviewId);
                review.setLikesCount(review.getLikesCount() - 1);

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
                    throw new ResourceAlreadyExistsException("User with username " + username + " has already disliked this review.");
                }

                // Se l'utente aveva messo in precedenza like alla recensione, rimuovo il like e decremento il numero di likes della recensione
                if (user.getLikes().contains(reviewId)) {
                    user.getLikes().remove(reviewId);
                    review.setDislikesCount(review.getLikesCount() - 1);
                }

                // Aggiungo l'utente alla lista di chi ha messo dislike alla recensione e incremento il numero di dislikes della recensione
                user.getDislikes().add(reviewId);
                review.setLikesCount(review.getDislikesCount() + 1);

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
                throw new ResourceAlreadyExistsException("User with username " + username + " has not disliked this review.");
            }

            // Rimuovo l'utente dalla lista di chi ha messo dislike alla recensione e decremento il numero di dislikes della recensione
            user.getDislikes().remove(reviewId);
            review.setLikesCount(review.getDislikesCount() - 1);

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


    /// DELETE operations ///
    // Elimina un utente con un determinato username
    public void deleteUser(String username) throws ResourceNotFoundException {
        User targetUser = userRepository.findByLogin_Username(username).orElse(null);

        if (targetUser == null) {
            throw new ResourceNotFoundException("User with username \"" + username + "\" not deletable because user does not exists.");
        }

        // Controllo se l'utente ha scritto almeno una recensione
        if (targetUser.getReviews().size() > 0) {
            // Elimino tutte le review dell'utente nella collection "reviews"
            deleteReviewByUserId_Username(username);
        }

        userRepository.delete(targetUser);
    }

    //// END of crud operations ////
    ////////////////////////////////
    


    ////////////////////////////////
    /////// OTHER operations ///////
}
