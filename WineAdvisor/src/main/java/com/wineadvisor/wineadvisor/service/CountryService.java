package com.wineadvisor.wineadvisor.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.wineadvisor.wineadvisor.exception.BadRequestException;
import com.wineadvisor.wineadvisor.exception.ResourceAlreadyExistsException;
import com.wineadvisor.wineadvisor.exception.ResourceNotFoundException;
import com.wineadvisor.wineadvisor.model.countries.Country;
import com.wineadvisor.wineadvisor.model.utils.Currency;
import com.wineadvisor.wineadvisor.repository.CountryRepository;
import com.wineadvisor.wineadvisor.repository.WineRepository;
import com.wineadvisor.wineadvisor.repository.WineryRepository;
import com.wineadvisor.wineadvisor.DTO.countries.CreateCountryDTO;
import com.wineadvisor.wineadvisor.DTO.countries.UpdateCountryDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CountryService {
    ////////////////////////////////
    /////////// VARIABLES //////////
    ////////////////////////////////
    private final CountryRepository countryRepository;
    private final WineryRepository wineryRepository;
    private final WineRepository wineRepository;
    
    /////////// COSTANTI ////////////
    private static final int PAGE_SIZE = 20;



    ////////////////////////////////
    /////// PRIVATE METHODS ////////
    ////////////////////////////////
    
    ////////////////////////////////
    ///// Checking operations //////
    
    // Controlla che la pagina ritornata dalla repo sia valida e che sia consistente rispetto alle opzioni di paginazione richieste dal client.
    private void checkReturnedPage(Page<Country> page, String notFoundMessage) throws ResourceNotFoundException, BadRequestException {
        if (page.getTotalElements() == 0) {
            throw new ResourceNotFoundException(notFoundMessage);
        }
        if (page.getPageable().getPageNumber() >= page.getTotalPages()) {
            throw new BadRequestException("Page requested too high.");
        }
    }
    
    /// END of checking operations //
    /////////////////////////////////
    

    /////////////////////////////////
    ///// Operations on reviews /////
    
    // Ricerca i vini di una certa nazione e aggiorna correttamente il campo "currency" della nazione
    private void updateWine_Region_Country_CurrencyByCountryName(String targetName, Currency updatedCurrency) {
        wineRepository
            .findByRegion_Country_Name(targetName)
            .forEach(
                wine -> {
                    wine.getRegion().getCountry().setCurrency(updatedCurrency);
                    wineRepository.save(wine);
                }
            );
    }

    // Ricerca i vini di una certa nazione e aggiorna correttamente il campo "name" della nazione
    private void updateWine_Region_Country_NameByCountryName(String targetName, String updatedName) {
        wineRepository
            .findByRegion_Country_Name(targetName)
            .forEach(
                wine -> {
                    wine.getRegion().getCountry().setName(updatedName);
                    wineRepository.save(wine);
                }
            );
    }

    // Ricerca le cantine di una certa nazione e aggiorna correttamente il campo "name" della nazione
    private void updateWinery_CountryByCountryName(String targetName, String updatedName) {
        wineryRepository
            .findByCountry(targetName)
            .forEach(
                winery -> {
                    winery.setCountry(updatedName);
                    wineryRepository.save(winery);
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
    // Aggiunge una country alla collection "countries" del database
    public Country createCountry(CreateCountryDTO createCountryDTO) throws ResourceAlreadyExistsException {
        if (countryRepository.findByName(createCountryDTO.getName()).isPresent()) {
            throw new ResourceAlreadyExistsException("Country with name \"" + createCountryDTO.getName() + "\" already exists.");
        }

        Country country = createCountryDTO.toCountry();

        return countryRepository.save(country);
    }


    /// READ operations ///
    // Restituisce tutte le nazioni presenti nella collection "countries" del database
    public Page<Country> getAllCountries(Integer page) throws ResourceNotFoundException, BadRequestException {
        Page<Country> countries = countryRepository.findAll(PageRequest.of(page, PAGE_SIZE));
        checkReturnedPage(countries, "No countries found.");
        return countries;
    }

    // Restituisce la nazione con il name specificato
    public Country getCountryByName(String name) throws ResourceNotFoundException {
        return countryRepository.findByName(name).orElseThrow(
            () -> new ResourceNotFoundException("Country with name \"" + name + "\" not found.")
        );
    }
    

    /// UPDATE operations ///
    // Cerca il documento di una nazione con un determinato name e lo aggiorna con i dati passati nel DTO
    public Country updateCountry(String name, UpdateCountryDTO updateCountryDTO) throws ResourceNotFoundException {
        return countryRepository
            .findByName(name)
            .map(
                targetCountry -> {
                    targetCountry = updateCountryDTO.toCountry(targetCountry);

                    // Aggiorno tutti i vini di quella nazione nella collection "wines"
                    updateWine_Region_Country_CurrencyByCountryName(name, targetCountry.getCurrency());

                    return countryRepository.save(targetCountry);
                }
            )
            .orElseThrow(
                () -> new ResourceNotFoundException("Country with name \"" + name + "\" not found.")
            );
    }

    // Cerca il documento di una nazione e ne modifica il name
    public Country updateCountryName(String targetName, String newName) throws ResourceNotFoundException {
        if (targetName.equals(newName)) {
            throw new BadRequestException("Name not updatable because it is the same as the old one.");
        }
        if (countryRepository.findByName(newName).isPresent()) {
            throw new ResourceAlreadyExistsException("Name not updatable because \"" + newName + "\" is already used by another country.");
        }

        return countryRepository
            .findByName(targetName)
            .map(
                targetCountry -> {
                    targetCountry.setName(newName.trim());

                    // Aggiorno tutti i vini di quella nazione nella collection "wines"
                    updateWine_Region_Country_NameByCountryName(targetName, newName);

                    // Aggiorno tutte le cantine di quella nazione nella collection "wineries"
                    updateWinery_CountryByCountryName(targetName, newName);

                    return countryRepository.save(targetCountry);
                }
            )
            .orElseThrow(
                () -> new ResourceNotFoundException("Name \"" + targetName + "\" not updatable because no country uses it.")
            );
    }
    

    /// DELETE operations ///
    // Elmina tutte le nazioni presenti nella collection "countries" del database
    public void deleteAllCountries() {
        countryRepository.deleteAll();
    }

    // Elimina la nazione con il name specificato
    public void deleteCountry(String name) throws ResourceNotFoundException {
        Country country = countryRepository.findByName(name).orElseThrow(
            () -> new ResourceNotFoundException("Country with name \"" + name + "\" not found.")
        );

        countryRepository.delete(country);
    }
    
    //// END of crud operations /////
    /////////////////////////////////
}
