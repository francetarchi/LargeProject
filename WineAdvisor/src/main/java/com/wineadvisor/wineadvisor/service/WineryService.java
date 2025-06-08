package com.wineadvisor.wineadvisor.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import com.wineadvisor.wineadvisor.DTO.utils.PasswordDTO;
import com.wineadvisor.wineadvisor.DTO.wineries.CreateWineryDTO;
import com.wineadvisor.wineadvisor.DTO.wineries.UpdateWineryDTO;
import com.wineadvisor.wineadvisor.exception.BadRequestException;
import com.wineadvisor.wineadvisor.exception.ResourceAlreadyExistsException;
import com.wineadvisor.wineadvisor.exception.ResourceNotFoundException;
import com.wineadvisor.wineadvisor.model.utils.ReviewEmbedded;
import com.wineadvisor.wineadvisor.model.wineries.Winery;
import com.wineadvisor.wineadvisor.repository.AdminRepository;
import com.wineadvisor.wineadvisor.repository.ReviewRepository;
import com.wineadvisor.wineadvisor.repository.UserRepository;
import com.wineadvisor.wineadvisor.repository.WineRepository;
import com.wineadvisor.wineadvisor.repository.WineryNeo4jRepository;
import com.wineadvisor.wineadvisor.repository.WineryRepository;

import lombok.RequiredArgsConstructor;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WineryService {
    ////////////////////////////////
    /////////// VARIABLES //////////
    ////////////////////////////////
    private final UserRepository userRepository;
    private final WineryRepository wineryRepository;
    private final AdminRepository adminRepository;
    private final WineRepository wineRepository;
    private final ReviewRepository reviewRepository;
    private final WineryNeo4jRepository wineryNeo4jRepository;

    private final PasswordEncoder passwordEncoder = PasswordDTO.passwordEncoder();

    /////////// COSTANTI ///////////
    private static final int PAGE_SIZE = 20;


    
    ////////////////////////////////
    /////// PRIVATE METHODS ////////
    ////////////////////////////////
    
    ////////////////////////////////
    ///// Checking operations //////
    
    // Controlla che la pagina ritornata dalla repo sia valida e che sia consistente rispetto alle opzioni di paginazione richieste dal client
    private void checkReturnedPage(Page<Winery> wineries, String notFoundMessage) throws ResourceNotFoundException, BadRequestException {
        if (wineries.getTotalElements() == 0) {
            throw new ResourceNotFoundException(notFoundMessage);
        }
        if (wineries.getPageable().getPageNumber() >= wineries.getTotalPages()) {
            throw new BadRequestException("Page requested too high.");
        }
    }

    // Controlla che i parametri passati per la creazione di una winery siano validi
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
    ////// Operations on users //////

    // Ricerca una review nella collection users (per ogni user, ho le reviews) e la elimina
    private void deleteUser_ReviewByReviewId(Long targetReviewId) {
        userRepository
            .findByReviews_ReviewId(targetReviewId)
            .map(
                user -> {
                    for (ReviewEmbedded r : user.getReviews()) {
                        if (r.getReviewId().equals(targetReviewId)) {
                            user.getReviews().remove(r);
                            break;
                        }
                    }

                    return userRepository.save(user);
                }
            );
    }
    
    //// END of operat. on users ////
    /////////////////////////////////
    

    /////////////////////////////////
    ///// Operations on reviews /////
    
    // Ricerca le review di un certo vino nella collection reviews e le elimina
    private void deleteReviewByWineId(Long targetWineId) {
        reviewRepository
            .findByWineId_Id(targetWineId)
            .forEach(
                review -> {
                    reviewRepository.delete(review);

                    // Elimino la review dalla collection "users" (qualora sia presente)
                    deleteUser_ReviewByReviewId(review.getId());
                }
            );
    }

    //// END of oper. on reviews ////
    /////////////////////////////////
    

    /////////////////////////////////
    ////// Operations on wines //////
    
    // Ricerca i vini di una certa winery nella collection wines e ne aggiorna correttamente il nome della winery
    private void updateWine_Winery_NameAndWine_Winery_ThumbnailByWineryUsername(String targetUsername, String updatedName, String updatedThumbnail) {
        wineRepository
            .findByWinery_Username(targetUsername)
            .forEach(
                wine -> {
                    wine.getWinery().setName(updatedName);
                    wine.getWinery().setThumbnail(updatedThumbnail);

                    wineRepository.save(wine);
                }
            );
    }

    // Ricerca i vini di una certa winery nella collection wines e ne aggiorna correttamente lo username della winery
    private void updateWine_Winery_UsernameByWineryUsername(String targetUsername, String updatedUsername) {
        wineRepository
            .findByWinery_Username(targetUsername)
            .forEach(
                wine -> {
                    wine.getWinery().setUsername(updatedUsername);
                    
                    wineRepository.save(wine);
                }
            );
    }

    // Ricerca i vini appartenenti ad una certa winery nella collection wines e li elimina
    private void deleteWineByWineryUsername(String targetUsername) {
        wineRepository
            .findByWinery_Username(targetUsername)
            .forEach(
                wine -> {
                    wineRepository.delete(wine);

                    // Elimino tutte le review fatte su questo vino dalla collection reviews
                    deleteReviewByWineId(wine.getId());
                }
            );
    }

    //// END of operat. on wines ////
    /////////////////////////////////

    

    /////////////////////////////////
    //////// PUBLIC METHODS /////////
    /////////////////////////////////
    
    /////////////////////////////////
    /////// CRUD operations /////////
    
    /// CREATE operations ///
    // Aggiunge una winery alla collection "wineries" del database
    @Transactional
    public Object createWinery(CreateWineryDTO createWineryDTO) throws ResourceAlreadyExistsException, BadRequestException {
		checkAccountParams(createWineryDTO.getUsername(), createWineryDTO.getEmail(), createWineryDTO.getPasswordDTO());
        
        Winery newWinery = createWineryDTO.toWinery();
        newWinery.adjustFieldsForCreation(passwordEncoder.encode(createWineryDTO.getPasswordDTO().getNewPass()));
        
        Winery savedWinery = wineryRepository.save(newWinery);

        // Sincronizzazione con Neo4j
        wineryNeo4jRepository.createWinery(newWinery.getLogin().getUsername(), newWinery.getName(), newWinery.getPicture().getThumbnail());

        return savedWinery;
	}


    /// READ operations ///
    // Restituisce tutte le wineries presenti nella collection "wineries" del database
    public Page<Winery> getAllWineries(Integer page) throws ResourceNotFoundException, BadRequestException {
        Page<Winery> wineries = wineryRepository.findAll(PageRequest.of(page, PAGE_SIZE));
        checkReturnedPage(wineries, "No wineries found.");
        return wineries;
    }

    // Restituisce tutte le wineries che contengono una certa stringa nel nome
    public Page<Winery> getWineriesByName(String name, Integer page) throws ResourceNotFoundException, BadRequestException {
        Page<Winery> wineries = wineryRepository.findByNameContainingIgnoreCase(name, PageRequest.of(page, PAGE_SIZE));
        checkReturnedPage(wineries, "Wineries with name containing \"" + name + "\" not found.");
        return wineries;
    }

    // Restituisce una winery con un determinato username
    public Winery getWineryByUsername(String username) throws ResourceNotFoundException {
        Winery winery = wineryRepository.findByLogin_Username(username).orElse(null);
        
        if (winery == null) {
            throw new ResourceNotFoundException("Winery with username \"" + username + "\" not found.");
        }

        return winery;
    }
    
    public Map<String, Object> getWineryFromGraph(String username) {
        return wineryNeo4jRepository.findWineryByUsername(username);
    }


    /// UPDATE operations ///
    // Cerca il documento di una winery con un determinato username e aggiorna l'intero documento con il nuovo passato come argomento
    @Transactional
    public Winery updateWinery(String targetUsername, UpdateWineryDTO updateWineryDTO) throws ResourceNotFoundException, ResourceAlreadyExistsException {
        return wineryRepository
            .findByLogin_Username(targetUsername)
            .map(
                targetWinery -> {
                    Winery wineryWithSameEmail = wineryRepository.findByEmail(updateWineryDTO.getEmail()).orElse(null);
                    if (wineryWithSameEmail != null && !wineryWithSameEmail.getLogin().getUsername().equals(targetWinery.getLogin().getUsername())) {
                        throw new ResourceAlreadyExistsException("Winery with username \"" + targetWinery.getLogin().getUsername() + "\" not updatable because email \"" + updateWineryDTO.getEmail() + "\" is already used by another winery.");
                    }

                    targetWinery = updateWineryDTO.toWinery(targetWinery);

                    // Aggiorno tutti i vini della winery nella collection "wines"
                    updateWine_Winery_NameAndWine_Winery_ThumbnailByWineryUsername(targetWinery.getLogin().getUsername(), targetWinery.getName(), targetWinery.getPicture().getThumbnail());
                    
                    // Finalizzo gli aggiornamenti in modo da evitare incosistenze nel database
                    targetWinery.adjustFieldsForUpdate();

                    Winery savedWinery = wineryRepository.save(targetWinery);

                    // Sincronizzazione con Neo4j
                    wineryNeo4jRepository.updateWinery(targetWinery.getLogin().getUsername(), targetWinery.getName(), targetWinery.getPicture().getThumbnail());

                    return savedWinery;
                }
            )
            .orElseThrow(
                () -> new ResourceNotFoundException("Winery with username \"" + targetUsername + "\" not updatable because it does not exist.")
            );
    }

    // Cerca il documento di una winery e ne modifica lo username
    @Transactional
    public Winery updateWineryUsername(String targetUsername, String newUsername) throws ResourceNotFoundException, ResourceAlreadyExistsException, BadRequestException {
        if (targetUsername.equals(newUsername)) {
            throw new BadRequestException("Username not updatable because it is the same as the old one.");
        }
        if (wineryRepository.findByLogin_Username(newUsername).isPresent()) {
            throw new ResourceAlreadyExistsException("Username not updatable because \"" + newUsername + "\" is already used by another winery.");
        }

        return wineryRepository
            .findByLogin_Username(targetUsername)
            .map(
                targetWinery -> {
                    targetWinery.getLogin().setUsername(newUsername.trim());

                    // Aggiorno tutti i vini della winery nella collection "wines"
                    updateWine_Winery_UsernameByWineryUsername(targetWinery.getLogin().getUsername(), newUsername.trim());

                    Winery savedWinery = wineryRepository.save(targetWinery);

                    // Sincronizzazione con Neo4j
                    wineryNeo4jRepository.updateWineryUsername(targetUsername, newUsername.trim());

                    return savedWinery;
                }
            )
            .orElseThrow(
                () -> new ResourceNotFoundException("Winery with username \"" + targetUsername + "\" not updatable because it does not exist.")
            );
    }

    // Cerca il documento di una winery e ne modifica la password
    public Winery updateWineryPassword(String targetUsername, PasswordDTO passwordDTO) throws IllegalArgumentException, ResourceNotFoundException {
        return wineryRepository
            .findByLogin_Username(targetUsername)
            .map(
                targetWinery -> {
                    if (!passwordEncoder.matches(passwordDTO.getOldPass(), targetWinery.getLogin().getPassword())) {
                        throw new IllegalArgumentException("Password of winery with username \"" + targetUsername + "\" not updatable because old password is wrong.");
                    }
                    if (passwordDTO.getNewPass().equals(passwordDTO.getOldPass())) {
                        throw new IllegalArgumentException("Password of winery with username \"" + targetUsername + "\" not updatable because new password is the same as the old one.");
                    }
                    if (!passwordDTO.getNewPass().equals(passwordDTO.getConfirmPass())) {
                        throw new IllegalArgumentException("Password of winery with username \"" + targetUsername + "\" not updatable because new passwords do not match.");
                    }
                    if (!passwordDTO.passwordPatternVerifier()) {
                        throw new IllegalArgumentException("Password does not meet the minimum requirements: at least 8 characters, 1 digit, 1 lowercase, 1 uppercase, 1 special character among \"!@#$%^&*()\\-_=+.,:;");
                    }

                    targetWinery.getLogin().setPassword(passwordEncoder.encode(passwordDTO.getNewPass()));
                    
                    return wineryRepository.save(targetWinery);
                }
            )
            .orElseThrow(
                () -> new ResourceNotFoundException("Winery with username \"" + targetUsername + "\" not updatable because it does not exist.")
            );
    }

    // Cerca il documento di una winery e aggiunge una foto alla gallery della winery
    public Winery addImage(String targetUsername, String image) throws ResourceNotFoundException, ResourceAlreadyExistsException {
        return wineryRepository
            .findByLogin_Username(targetUsername)
            .map(
                targetWinery -> {
                    if (targetWinery.getImages().contains(image)) {
                        throw new ResourceAlreadyExistsException("Image \"" + image + "\" already exists in the gallery of winery with username \"" + targetUsername + "\".");
                    }

                    targetWinery.getImages().add(image);

                    return wineryRepository.save(targetWinery);
                }
            )
            .orElseThrow(
                () -> new ResourceNotFoundException("Winery with username \"" + targetUsername + "\" not updatable because it does not exist.")
            );
    }

    // Cerca il documento di una winery e rimuove una foto dalla gallery della winery
    public Winery removeImage(String targetUsername, String image) throws ResourceNotFoundException {
        return wineryRepository
            .findByLogin_Username(targetUsername)
            .map(
                targetWinery -> {
                    if (!targetWinery.getImages().contains(image)) {
                        throw new ResourceNotFoundException("Image \"" + image + "\" not found in the gallery of winery with username \"" + targetUsername + "\".");
                    }

                    targetWinery.getImages().remove(image);

                    return wineryRepository.save(targetWinery);
                }
            )
            .orElseThrow(
                () -> new ResourceNotFoundException("Winery with username \"" + targetUsername + "\" not updatable because it does not exist.")
            );
    }

    /// DELETE operations ///
    // Elimina tutte le wineries dalla collection "wineries"
    @Transactional
    public void deleteAllWineries() {
        wineryRepository.deleteAll();

        // Sincronizzazione con Neo4j
        wineryNeo4jRepository.deleteAllWineries();
    }

    // Elimina una winery con un determinato username
    @Transactional
    public void deleteWinery(String targetUsername) throws ResourceNotFoundException {
        final Winery targetWinery = wineryRepository
            .findByLogin_Username(targetUsername)
            .orElseThrow(
                () -> new ResourceNotFoundException("Winery with username \"" + targetUsername + "\" not deletable because it does not exist.")
            );

        // Elimino tutti i vini della winery nella collection "wines"
        deleteWineByWineryUsername(targetUsername);
        
        wineryRepository.delete(targetWinery);

        // Sincronizzazione con Neo4j
        wineryNeo4jRepository.deleteWineryByUsername(targetUsername);
    }

    //// END of crud operations ////
    ////////////////////////////////
}
