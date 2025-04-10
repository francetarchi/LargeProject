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
import com.wineadvisor.wineadvisor.model.Review;
import com.wineadvisor.wineadvisor.model.User;
import com.wineadvisor.wineadvisor.model.Wine;
import com.wineadvisor.wineadvisor.model.fields.ReviewEmbedded;

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
    ////// METHODS (services) //////
    ////////////////////////////////

    ////////////////////////////////
    /////// CRUD operations ////////

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
                    }

                    targetUser.adjustFieldsForUpdate();

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
