package com.wineadvisor.wineadvisor.service;

import java.util.ArrayList;

import lombok.RequiredArgsConstructor;

import org.apache.coyote.BadRequestException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.wineadvisor.wineadvisor.config.PasswordUtils;
import com.wineadvisor.wineadvisor.exception.ResourceAlreadyExistsException;
import com.wineadvisor.wineadvisor.exception.ResourceNotFoundException;
import com.wineadvisor.wineadvisor.repository.ReviewRepository;
import com.wineadvisor.wineadvisor.repository.UserRepository;
import com.wineadvisor.wineadvisor.repository.WineRepository;
import com.wineadvisor.wineadvisor.model.Review;
import com.wineadvisor.wineadvisor.model.User;
import com.wineadvisor.wineadvisor.model.Wine;
import com.wineadvisor.wineadvisor.model.fields.users.ReviewEmbedded;

@Service
@RequiredArgsConstructor
public class UserService {
    ////////////////////////////////
    /////////// VARIABLES //////////
    ////////////////////////////////
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final WineRepository wineRepository;
    private final PasswordEncoder passwordEncoder = PasswordUtils.passwordEncoder();


    
    ////////////////////////////////
    ////// METHODS (services) //////
    ////////////////////////////////

    ////////////////////////////////
    /////// CRUD operations ////////

    /// CREATE operations ///
    // Aggiunge un utente alla collection "users" del database
    public User addUser(User newUser) throws ResourceAlreadyExistsException, BadRequestException {
        if (userRepository.findByLogin_Username(newUser.getLogin().getUsername()).isPresent()) {
            throw new ResourceAlreadyExistsException("User with username \"" + newUser.getLogin().getUsername() + "\" already exists.");
        }
        if (userRepository.findByEmail(newUser.getEmail()).isPresent()) {
            throw new ResourceAlreadyExistsException("User with email \"" + newUser.getEmail() + "\" already exists.");
        }
        if (!PasswordUtils.passwordPatternVerifier(newUser.getLogin().getPassword())) {
            throw new BadRequestException("Password does not meet the minimum requirements: at least 8 characters, 1 digit, 1 lowercase, 1 uppercase, 1 special character among \"!@#$%^&*()-_=+\".");
        }

        newUser.set_id(null);
        newUser.getLogin().setPassword(passwordEncoder.encode(newUser.getLogin().getPassword()));
        return userRepository.save(newUser);
    }
    
    
    /// READ operations ///
    // Restituisce tutti gli utenti presenti nella collection "users" del database
    public ArrayList<User> getAllUsers() throws ResourceNotFoundException {
        ArrayList<User> users = (ArrayList<User>) userRepository.findAll();

        if (users.size() == 0) {
            throw new ResourceNotFoundException("No users found.");
        }

        return new ArrayList<>(users.subList(0, 9));
    }

    // Restituisce tutti gli utenti con un determinato nome
    public ArrayList<User> getUsersByFirstName(String firstName) throws ResourceNotFoundException {
        ArrayList<User> result = userRepository.findByName_First(firstName);
        
        if (result.isEmpty()) {
            throw new ResourceNotFoundException("Users with first name \"" + firstName + "\" not found.");
        }

        return result;
    }

    // Restituisce tutti gli utenti con un determinato cognome
    public ArrayList<User> getUsersByLastName(String lastName) throws ResourceNotFoundException {
        ArrayList<User> result = userRepository.findByName_Last(lastName);
        
        if (result.isEmpty()) {
            throw new ResourceNotFoundException("Users with last name \"" + lastName + "\" not found.");
        }
        
        return result;
    }

    // Restituisce tutti gli utenti con un determinato nome e cognome
    public ArrayList<User> getUsersByFullName(String firstName, String lastName) throws ResourceNotFoundException {
        ArrayList<User> result = userRepository.findByName_Last(lastName);
        result.removeIf(user -> !user.getName().getFirst().equals(firstName));

        if (result.isEmpty()) {
            throw new ResourceNotFoundException("Users with first name \"" + firstName + "\" and last name \"" + lastName + "\" not found.");
        }

        return result;
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
        if (userRepository.findByEmail(updatedUser.getEmail()).isPresent()) {
            throw new ResourceAlreadyExistsException("User with username \"" + updatedUser.getLogin().getUsername() + "\" not updatable because email \"" + updatedUser.getEmail() + "\" is already used by another user.");
        }

        final User user = userRepository
            .findByLogin_Username(updatedUser.getLogin().getUsername())
            .map(
                targetUser -> {
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
                    }

                    return userRepository.save(targetUser);
                }
            )
            .orElseThrow(
                () -> new ResourceNotFoundException("User with username \"" + updatedUser.getLogin().getUsername() + "\" not updatable because it does not exist.")
            );

        // // Controllo se l'utente ha scritto almeno una recensione
        // if (!user.getReviews().isEmpty()) {
        //     reviewRepository
        //         .findByUserId_Username(user.getLogin().getUsername())
        //         .forEach(
        //             review -> {
        //                 // Aggiorno la recensione nella collection "reviews"
        //                 review.getUserId().setThumbnail(user.getPicture().getThumbnail());
        //                 reviewRepository.save(review);

        //                 // Aggiorno la recensione nel vino (qualora sia presente)
        //                 Long reviewId = review.getId();
        //                 Integer year = review.getWineId().getYear();
                        
        //                 Wine wine = wineRepository.findByVintages_Reviews_Review_id(reviewId).orElse(null);
        //                 if (wine != null) {
        //                     for (Vintage v : wine.getVintages()) {
        //                         if (v.getYear() == year) {
        //                             for (Review r : v.getReviews()) {
        //                                 if (r.getId() == reviewId) {
        //                                     r.getUserId().setThumbnail(user.getPicture().getThumbnail());
        //                                     break;
        //                                 }
        //                             }
        //                             break;
        //                         }
        //                     }
        //                 }

        //                 wineRepository.save(wine);
        //             }
        //         );
        // }

        return user;
    }

    // Cerca il documento di un utente e ne modifica lo username
    public Object updateUserUsername(String targetUsername, String newUsername) throws ResourceNotFoundException, ResourceAlreadyExistsException{
        if (userRepository.findByLogin_Username(newUsername).isPresent()) {
            throw new ResourceAlreadyExistsException("Username not updatable because \"" + newUsername + "\" is already used by another user.");
        }

        final User user = userRepository
            .findByLogin_Username(targetUsername)
            .map(
                targetUser -> {
                    targetUser.getLogin().setUsername(newUsername);
                    return userRepository.save(targetUser);
                }
            )
            .orElseThrow(
                () -> new ResourceNotFoundException("Username not updatable because \"" + targetUsername + "\" no user uses it.")
            );

        // // Controllo se l'utente ha scritto almeno una recensione
        // if (!user.getReviews().isEmpty()) {
        //     reviewRepository
        //         .findByUserId_Username(targetUsername)
        //         .forEach(
        //             review -> {
        //                 // Aggiorno la recensione nella collection "reviews"
        //                 review.getUserId().setUsername(user.getLogin().getUsername());
        //                 reviewRepository.save(review);

        //                 // Aggiorno la recensione nel vino (qualora sia presente)
        //                 Long reviewId = review.getId();
        //                 Integer year = review.getWineId().getYear();
                        
        //                 Wine wine = wineRepository.findByVintages_Reviews_Review_id(reviewId).orElse(null);
        //                 if (wine != null) {
        //                     for (Vintage v : wine.getVintages()) {
        //                         if (v.getYear() == year) {
        //                             for (Review r : v.getReviews()) {
        //                                 if (r.getId() == reviewId) {
        //                                     r.getUserId().setUsername(user.getLogin().getUsername());
        //                                     break;
        //                                 }
        //                             }
        //                             break;
        //                         }
        //                     }
        //                 }

        //                 wineRepository.save(wine);
        //             }
        //         );
        // }
        
        return user;
    }

    // Cerca il documento di un utente e ne modifica la password
    public User updateUserPassword(String username, String oldPass, String newPass, String confirmPass) throws IllegalArgumentException, ResourceNotFoundException {
        return userRepository
            .findByLogin_Username(username)
            .map(
                targetUser -> {
                    if (!passwordEncoder.matches(oldPass, targetUser.getLogin().getPassword())) {
                        throw new IllegalArgumentException("Password of user with username \"" + username + "\" not updatable because old password is wrong.");
                    }
                    if (newPass.equals(oldPass)) {
                        throw new IllegalArgumentException("Password of user with username \"" + username + "\" not updatable because new password is the same as the old one.");
                    }
                    if (!newPass.equals(confirmPass)) {
                        throw new IllegalArgumentException("Password of user with username \"" + username + "\" not updatable because new passwords do not match.");
                    }
                    if (!PasswordUtils.passwordPatternVerifier(newPass)) {
                        throw new IllegalArgumentException("Password does not meet the minimum requirements: at least 8 characters, 1 digit, 1 lowercase, 1 uppercase, 1 special character among \"!@#$%^&*()-_=+\".");
                    }

                    targetUser.getLogin().setPassword(passwordEncoder.encode(newPass));
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

        userRepository.delete(targetUser);

        // // Controllo se l'utente ha scritto almeno una recensione
        // if (!targetUser.getReviews().isEmpty()) {
        //     reviewRepository
        //         .findByUserId_Username(username)
        //         .forEach(
        //             review -> {
        //                 // Elimino la recensione dalla collection "reviews"
        //                 reviewRepository.delete(review);

        //                 // Elimino la recensione dal vino (qualora sia presente)
        //                 Long reviewId = review.getId();
        //                 Integer year = review.getWineId().getYear();
                            
        //                 Wine wine = wineRepository.findByVintages_Reviews_Review_id(reviewId).orElse(null);
        //                 if (wine != null) {
        //                     for (Vintage v : wine.getVintages()) {
        //                         if (v.getYear() == year) {
        //                             for (Review r : v.getReviews()) {
        //                                 if (r.getId() == reviewId) {
        //                                     v.getReviews().remove(r);
        //                                     break;
        //                                 }
        //                             }
        //                             break;
        //                         }
        //                     }
        //                 }

        //                 wineRepository.save(wine);
        //             }
        //         );
        // }
    }



    //// END of crud operations ////
    ////////////////////////////////
    


    ////////////////////////////////
    /////// OTHER operations ///////
}
