package com.wineadvisor.wineadvisor.service;

import java.util.ArrayList;

import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.wineadvisor.wineadvisor.config.PasswordUtils;
import com.wineadvisor.wineadvisor.exception.ResourceNotFoundException;
import com.wineadvisor.wineadvisor.repository.UserRepository;
import com.wineadvisor.wineadvisor.model.User;

import lombok.RequiredArgsConstructor;

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
    public User addUser(User newUser) {
        if (newUser == null) {
            throw new IllegalArgumentException("Given user is empty");
        }
        // if (userRepository.existsById(newUser.get_id())) {
        //     throw new IllegalArgumentException("User with id \"" + newUser.get_id() + "\" already exists");
        // }
        if (userRepository.findByLogin_Username(newUser.getLogin().getUsername()).isPresent()) {
            throw new IllegalArgumentException("User with username \"" + newUser.getLogin().getUsername() + "\" already exists.");
        }
        if (!PasswordUtils.passwordPatternVerifier(newUser.getLogin().getPassword())) {
            throw new IllegalArgumentException("Password does not meet the minimum requirements: at least 8 characters, 1 digit, 1 lowercase, 1 uppercase, 1 special character among \"!@#$%^&*()-_=+\".");
        }

        newUser.set_id(null);
        newUser.getLogin().setPassword(passwordEncoder.encode(newUser.getLogin().getPassword()));
        return userRepository.save(newUser);
    }

    // Aggiunge più utenti alla collection "users" del database
    // public ArrayList<User> addUsers(ArrayList<User> newUsers) {
    //     if (newUsers.isEmpty()) {
    //         throw new IllegalArgumentException("Given users list is empty");
    //     }
    //     for (User user : newUsers) {
    //         if (userRepository.existsById(user.get_id())) {
    //             throw new IllegalArgumentException("User with id \"" + user.get_id() + "\" already exists.");
    //         }
    //         if (userRepository.findByLogin_Username(user.getLogin().getUsername()).isPresent()) {
    //             throw new IllegalArgumentException("User with username \"" + user.getLogin().getUsername() + "\" already exists.");
    //         }
    //     }
    //     return (ArrayList<User>) userRepository.saveAll(newUsers);
    // }
    
    
    /// READ operations ///
    // Restituisce tutti gli utenti presenti nella collection "users" del database
    public ArrayList<User> getAllUsers() throws NotFoundException {
        ArrayList<User> users = (ArrayList<User>) userRepository.findAll();

        if (users.size() == 0) {
            throw new NotFoundException();
        }

        return new ArrayList<>(users.subList(0, 9));
    }

    /////////////////////// Commentata perché non so se tenerla: nella collection "users" il campo unique che usiamo è il login.username //////////////////////
    // // Restituisce un utente con un determinato id
    // public User getUserById(Long id) {
    //     return userRepository.findById(id).orElse(null);
    // }

    // Restituisce un utente con un determinato username
    public User getUserByUsername(String username) {
        User user = userRepository.findByLogin_Username(username).orElse(null);
        
        if (user == null) {
            throw new IllegalArgumentException("User with username \"" + username + "\" not found.");
        }

        return user;
    }

    // Restituisce tutti gli utenti con un determinato nome
    public ArrayList<User> getUsersByFirstName(String first_name) {
        ArrayList<User> result = userRepository.findByName_First(first_name);
        
        if (result.isEmpty()) {
            throw new IllegalArgumentException("Users with first name \"" + first_name + "\" not found.");
        }

        return result;
    }

    // Restituisce tutti gli utenti con un determinato cognome
    public ArrayList<User> getUsersByLastName(String last_name) {
        ArrayList<User> result = userRepository.findByName_Last(last_name);
        
        if (result.isEmpty()) {
            throw new IllegalArgumentException("Users with last name \"" + last_name + "\" not found.");
        }
        
        return result;
    }


    /// UPDATE operations ///
    // Cerca il documento di un utente con un determinato username e aggiorna l'intero documento con il nuovo passato come argomento
    public User updateUser(User updatedUser) {
        return userRepository
            .findByLogin_Username(updatedUser.getLogin().getUsername())
            .map(targetUser -> {
                targetUser.setName(updatedUser.getName());
                targetUser.setLocation(updatedUser.getLocation());
                targetUser.setEmail(updatedUser.getEmail());
                targetUser.setTelephone(updatedUser.getTelephone());
                targetUser.setDob(updatedUser.getDob());
                targetUser.setPicture(updatedUser.getPicture());

                return userRepository.save(targetUser);
            })
            .orElseThrow(() -> new ResourceNotFoundException("User with username \"" + updatedUser.getLogin().getUsername() + "\" not updatable because it does not exist."));
    }

    // Cerca il documento di un utente e ne modifica la password
    public User updateUserPassword(String username, String oldPass, String newPass, String confirmPass) {
        return userRepository
            .findByLogin_Username(username)
            .map(targetUser -> {
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
            })
            .orElseThrow(() -> new ResourceNotFoundException("User with username \"" + username + "\" not updatable because it does not exist."));
    }


    /// DELETE operations ///
    // Elimina un utente con un determinato username
    public void deleteUser(String username) {
        User targetUser = userRepository.findByLogin_Username(username).orElse(null);

        if (targetUser == null) {
            throw new IllegalArgumentException("User with username \"" + username + "\" not deletable because user does not exists.");
        }

        userRepository.delete(targetUser);
    }


    //// END of crud operations ////
    ////////////////////////////////
    


    ////////////////////////////////
    /////// OTHER operations ///////
}
