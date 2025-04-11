package com.wineadvisor.wineadvisor.service;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.wineadvisor.wineadvisor.DTO.PasswordDTO;
import com.wineadvisor.wineadvisor.exception.ResourceAlreadyExistsException;
import com.wineadvisor.wineadvisor.exception.ResourceNotFoundException;
import com.wineadvisor.wineadvisor.exception.BadRequestException;
import com.wineadvisor.wineadvisor.repository.ReviewRepository;
import com.wineadvisor.wineadvisor.repository.UserRepository;
import com.wineadvisor.wineadvisor.repository.WineRepository;
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
    public User addUser(User newUser, PasswordDTO passwordDTO) throws ResourceAlreadyExistsException, BadRequestException {
        if (userRepository.findByLogin_Username(newUser.getLogin().getUsername()).isPresent()) {
            throw new ResourceAlreadyExistsException("User with username \"" + newUser.getLogin().getUsername() + "\" already exists.");
        }
        if (userRepository.findByEmail(newUser.getEmail()).isPresent()) {
            throw new ResourceAlreadyExistsException("User with email \"" + newUser.getEmail() + "\" already exists.");
        }
        if (!passwordDTO.passwordPatternVerifier()) {
            throw new BadRequestException("Password does not meet the minimum requirements: at least 8 characters, 1 digit, 1 lowercase, 1 uppercase, 1 special character among \"!@#$%^&*()\\-_=+.,:;");
        }
        if (!passwordDTO.getNewPass().equals(passwordDTO.getConfirmPass())) {
            throw new BadRequestException("Passwords do not match.");
        }

        newUser.adjustFieldsForCreation(passwordEncoder.encode(passwordDTO.getNewPass()));

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
    public User updateUser(User updatedUser) throws ResourceNotFoundException, ResourceAlreadyExistsException {
        final User user = userRepository
            .findByLogin_Username(updatedUser.getLogin().getUsername())
            .map(
                targetUser -> {
                    User userWithSameEmail = userRepository.findByEmail(updatedUser.getEmail()).orElse(null);
                    if ( userWithSameEmail != null && !userWithSameEmail.getLogin().getUsername().equals(targetUser.getLogin().getUsername()) ) {
                        throw new ResourceAlreadyExistsException("User with username \"" + updatedUser.getLogin().getUsername() + "\" not updatable because email \"" + updatedUser.getEmail() + "\" is already used by another user.");
                    }
                    
                    targetUser.setName(updatedUser.getName());
                    targetUser.setLocation(updatedUser.getLocation());
                    targetUser.setEmail(updatedUser.getEmail());
                    targetUser.setTelephone(updatedUser.getTelephone());
                    targetUser.setDob(updatedUser.getDob());
                    targetUser.setPicture(updatedUser.getPicture());
                    if (targetUser.getReviews().size() > 0) {
                        for (ReviewEmbedded review : targetUser.getReviews()) {
                            review.getUserId().setThumbnail(updatedUser.getPicture().getThumbnail());
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
                () -> new ResourceNotFoundException("User with username \"" + updatedUser.getLogin().getUsername() + "\" not updatable because it does not exist.")
            );

        return user;
    }

    // Cerca il documento di un utente e ne modifica lo username
    public Object updateUserUsername(String targetUsername, String newUsername) throws ResourceNotFoundException, ResourceAlreadyExistsException, BadRequestException{
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
