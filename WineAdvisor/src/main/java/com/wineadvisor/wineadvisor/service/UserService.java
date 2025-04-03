package com.wineadvisor.wineadvisor.service;

import java.util.ArrayList;

import lombok.RequiredArgsConstructor;

import org.apache.coyote.BadRequestException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.wineadvisor.wineadvisor.config.PasswordUtils;
import com.wineadvisor.wineadvisor.exception.ResourceAlreadyExistsException;
import com.wineadvisor.wineadvisor.exception.ResourceNotFoundException;
import com.wineadvisor.wineadvisor.repository.UserRepository;
import com.wineadvisor.wineadvisor.model.User;

@Service
@RequiredArgsConstructor
public class UserService {
    ////////////////////////////////
    /////////// VARIABLES //////////
    ////////////////////////////////
    private final UserRepository userRepository;
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
            throw new ResourceAlreadyExistsException("User with email \"" + updatedUser.getEmail() + "\" already exists.");
        }

        return userRepository
            .findByLogin_Username(updatedUser.getLogin().getUsername())
            .map(
                targetUser -> {
                    targetUser.setName(updatedUser.getName());
                    targetUser.setLocation(updatedUser.getLocation());
                    targetUser.setEmail(updatedUser.getEmail());
                    targetUser.setTelephone(updatedUser.getTelephone());
                    targetUser.setDob(updatedUser.getDob());
                    targetUser.setPicture(updatedUser.getPicture());

                    return userRepository.save(targetUser);
                }
            )
            .orElseThrow(
                () -> new ResourceNotFoundException("User with username \"" + updatedUser.getLogin().getUsername() + "\" not updatable because it does not exist.")
            );
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
    }


    //// END of crud operations ////
    ////////////////////////////////
    


    ////////////////////////////////
    /////// OTHER operations ///////
}
