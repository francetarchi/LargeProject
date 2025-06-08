package com.wineadvisor.wineadvisor.service;

import com.wineadvisor.wineadvisor.exception.ResourceAlreadyExistsException;
import com.wineadvisor.wineadvisor.exception.ResourceNotFoundException;
import com.wineadvisor.wineadvisor.DTO.utils.PasswordDTO;
import com.wineadvisor.wineadvisor.DTO.admins.CreateAdminDTO;
import com.wineadvisor.wineadvisor.DTO.admins.UpdateAdminDTO;
import com.wineadvisor.wineadvisor.exception.BadRequestException;
import com.wineadvisor.wineadvisor.repository.AdminRepository;
import com.wineadvisor.wineadvisor.repository.UserRepository;
import com.wineadvisor.wineadvisor.repository.WineryRepository;
import com.wineadvisor.wineadvisor.model.admin.Admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class AdminService {
    ////////////////////////////////
    /////////// VARIABLES //////////
    ////////////////////////////////
    private final UserRepository userRepository;
    private final WineryRepository wineryRepository;
    private final AdminRepository adminRepository;

    private final PasswordEncoder passwordEncoder = PasswordDTO.passwordEncoder();

    /////////// COSTANTI ////////////
    private static final int PAGE_SIZE = 20;



    ////////////////////////////////
    /////// PRIVATE METHODS ////////
    ////////////////////////////////
    
    ////////////////////////////////
    ///// Checking operations //////
    
    // Controlla che la pagina ritornata dalla repo sia valida e che sia consistente rispetto alle opzioni di paginazione richieste dal client.
    private void checkReturnedPage(Page<Admin> page, String notFoundMessage) throws ResourceNotFoundException, BadRequestException {
        if (page.getTotalElements() == 0) {
            throw new ResourceNotFoundException(notFoundMessage);
        }
        if (page.getPageable().getPageNumber() >= page.getTotalPages()) {
            throw new BadRequestException("Page requested too high.");
        }
    }

    // Controlla che i parametri passati per la creazione di un admin siano validi
    private void checkAccountParams(String username, String email, PasswordDTO passwordDTO) throws ResourceAlreadyExistsException, BadRequestException {
        if (userRepository.findByLogin_Username(username).isPresent()) {
            throw new ResourceAlreadyExistsException("User with username \"" + username + "\" already exists.");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new ResourceAlreadyExistsException("User with email \"" + email + "\" already exists.");
        }
        if (wineryRepository.findByLogin_Username(username).isPresent()) {
            throw new ResourceAlreadyExistsException("Winery with username \"" + username + "\" already exists.");
        }
        if (wineryRepository.findByEmail(email).isPresent()) {
            throw new ResourceAlreadyExistsException("Winery with email \"" + email + "\" already exists.");
        }
        if (adminRepository.findByLogin_Username(username).isPresent()) {
            throw new ResourceAlreadyExistsException("Admin with username \"" + username + "\" already exists.");
        }
        if (adminRepository.findByEmail(email).isPresent()) {
            throw new ResourceAlreadyExistsException("Admin with email \"" + email + "\" already exists.");
        }
        if (!passwordDTO.passwordPatternVerifier()) {
            throw new BadRequestException("Password does not meet the minimum requirements: at least 8 characters, 1 digit, 1 lowercase, 1 uppercase, 1 special character among \"!@#$%^&*()\\-_=+.,:;");
        }
        if (!passwordDTO.getNewPass().equals(passwordDTO.getConfirmPass())) {
            throw new BadRequestException("Passwords do not match.");
        }
    }
    
    /// END of checking operations //
    /////////////////////////////////



    /////////////////////////////////
    //////// PUBLIC METHODS /////////
    /////////////////////////////////

    /////////////////////////////////
    /////// CRUD operations /////////

    /// CREATE operations ///
    // Aggiunge un admin alla collection "admins" del database
    public Admin createAdmin(CreateAdminDTO createAdminDTO) throws ResourceAlreadyExistsException, BadRequestException {
        checkAccountParams(createAdminDTO.getUsername(), createAdminDTO.getEmail(), createAdminDTO.getPasswordDTO());
        
        Admin newAdmin = createAdminDTO.toAdmin();
        newAdmin.adjustFieldsForCreation(passwordEncoder.encode(createAdminDTO.getPasswordDTO().getNewPass()));

        return adminRepository.save(newAdmin);
    }
    
    
    /// READ operations ///
    // Restituisce tutti gli admin presenti nella collection "admins" del database
    public Page<Admin> getAllAdmins(Integer page) throws ResourceNotFoundException, BadRequestException {
        Page<Admin> admins = adminRepository.findAll(PageRequest.of(page, PAGE_SIZE));
        checkReturnedPage(admins, "No admins found.");
        return admins;
    }

    // Restituisce un admin con un determinato username
    public Admin getAdminByUsername(String username) throws ResourceNotFoundException {
        Admin user = adminRepository.findByLogin_Username(username).orElseThrow(
            () -> new ResourceNotFoundException("Admin with username \"" + username + "\" not found.")
        );
        return user;
    }
    
    
    /// UPDATE operations ///
    // Cerca il documento di un admin con un determinato username e aggiorna l'intero documento con il nuovo passato come argomento
    public Admin updateAdmin(String targetUsername, UpdateAdminDTO updateAdminDTO) throws ResourceNotFoundException, ResourceAlreadyExistsException {
        return adminRepository
            .findByLogin_Username(targetUsername)
            .map(
                targetAdmin -> {
                    Admin adminWithSameEmail = adminRepository.findByEmail(updateAdminDTO.getEmail()).orElse(null);
                    if ( adminWithSameEmail != null && !adminWithSameEmail.getLogin().getUsername().equals(targetAdmin.getLogin().getUsername()) ) {
                        throw new ResourceAlreadyExistsException("Admin with username \"" + targetAdmin.getLogin().getUsername() + "\" not updatable because email \"" + updateAdminDTO.getEmail() + "\" is already used by another admin.");
                    }

                    targetAdmin = updateAdminDTO.toAdmin(targetAdmin);
                    targetAdmin.adjustFieldsForUpdate();

                    return adminRepository.save(targetAdmin);
                }
            )
            .orElseThrow(
                () -> new ResourceNotFoundException("Admin with username \"" + targetUsername + "\" not updatable because it does not exist.")
            );
    }

    // Cerca il documento di un admin e ne modifica lo username
    public Admin updateAdminUsername(String targetUsername, String newUsername) throws ResourceNotFoundException, ResourceAlreadyExistsException, BadRequestException{
        if (targetUsername.equals(newUsername)) {
            throw new BadRequestException("Username not updatable because it is the same as the old one.");
        }
        if (adminRepository.findByLogin_Username(newUsername).isPresent()) {
            throw new ResourceAlreadyExistsException("Username not updatable because \"" + newUsername + "\" is already used by another admin.");
        }

        return adminRepository
            .findByLogin_Username(targetUsername)
            .map(
                targetAdmin -> {
                    targetAdmin.getLogin().setUsername(newUsername.trim());
                    return adminRepository.save(targetAdmin);
                }
            )
            .orElseThrow(
                () -> new ResourceNotFoundException("Username \"" + targetUsername + "\" not updatable because no admin uses it.")
            );
    }

    // Cerca il documento di un admin e ne modifica la password
    public Admin updateAdminPassword(String targetUsername, PasswordDTO passwordDTO) throws IllegalArgumentException, ResourceNotFoundException {
        passwordDTO.setOldPass(passwordDTO.getOldPass().trim());
        passwordDTO.setNewPass(passwordDTO.getNewPass().trim());
        passwordDTO.setConfirmPass(passwordDTO.getConfirmPass().trim());

        return adminRepository
            .findByLogin_Username(targetUsername)
            .map(
                targetAdmin -> {
                    if (!passwordEncoder.matches(passwordDTO.getOldPass(), targetAdmin.getLogin().getPassword())) {
                        throw new IllegalArgumentException("Password of admin with username \"" + targetUsername + "\" not updatable because old password is wrong.");
                    }
                    if (passwordDTO.getNewPass().equals(passwordDTO.getOldPass())) {
                        throw new IllegalArgumentException("Password of admin with username \"" + targetUsername + "\" not updatable because new password is the same as the old one.");
                    }
                    if (!passwordDTO.getNewPass().equals(passwordDTO.getConfirmPass())) {
                        throw new IllegalArgumentException("Password of admin with username \"" + targetUsername + "\" not updatable because new passwords do not match.");
                    }
                    if (!passwordDTO.passwordPatternVerifier()) {
                        throw new IllegalArgumentException("Password does not meet the minimum requirements: at least 8 characters, 1 digit, 1 lowercase, 1 uppercase, 1 special character among \"!@#$%^&*()\\-_=+.,:;");
                    }

                    targetAdmin.getLogin().setPassword(passwordEncoder.encode(passwordDTO.getNewPass()));
                    
                    return adminRepository.save(targetAdmin);
                }
            )
            .orElseThrow(
                () -> new ResourceNotFoundException("Admin with username \"" + targetUsername + "\" not updatable because it does not exist.")
            );
    }
    
    
    /// DELETE operations ///
    // Elimina un admin con un determinato username
    public void deleteAdmin(String targetUsername) throws IllegalStateException, ResourceNotFoundException {
        // Se attualmente c'è un solo admin nel database, non è possibile eliminarlo
        if (adminRepository.findAll().size() == 1) {
            throw new IllegalStateException("Admin with username \"" + targetUsername + "\" not deletable because it is the only admin in the database.");
        }

        final Admin targetAdmin = adminRepository
            .findByLogin_Username(targetUsername)
            .orElseThrow(
                () -> new ResourceNotFoundException("Admin with username \"" + targetUsername + "\" not deletable because user does not exists.")
            );

        adminRepository.delete(targetAdmin);
    }
    
    //// END of crud operations ////
    ////////////////////////////////
}
