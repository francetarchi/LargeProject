package com.wineadvisor.wineadvisor.service;

import java.util.ArrayList;

import com.wineadvisor.wineadvisor.exception.ResourceAlreadyExistsException;
import com.wineadvisor.wineadvisor.exception.ResourceNotFoundException;
import com.wineadvisor.wineadvisor.model.regions.Region;
import com.wineadvisor.wineadvisor.model.countries.Country;
import com.wineadvisor.wineadvisor.model.users.User;
import com.wineadvisor.wineadvisor.model.wineries.Winery;
import com.wineadvisor.wineadvisor.model.wines.Wine;
import com.wineadvisor.wineadvisor.repository.CountryRepository;
import com.wineadvisor.wineadvisor.repository.RegionRepository;
import com.wineadvisor.wineadvisor.repository.UserRepository;
import com.wineadvisor.wineadvisor.repository.WineRepository;
import com.wineadvisor.wineadvisor.repository.WineryRepository;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RegionService {
    ////////////////////////////////
    /////////// VARIABLES //////////
    ////////////////////////////////
    private final RegionRepository regionRepository;
    private final CountryRepository countryRepository;
    private final UserRepository userRepository;
    private final WineryRepository wineryRepository;
    private final WineRepository wineRepository;



    ////////////////////////////////
    /////// PRIVATE METHODS ////////
    ////////////////////////////////

    // No private methods for now //



    ////////////////////////////////
    //////// PUBLIC METHODS ////////
    ////////////////////////////////
    
    /////////////////////////////////
    /////// CRUD operations /////////

    /// CREATE operations ///
    // Aggiunge una regione alla collection "regions" del database
    public Region addRegion(String name, String country) {
        // Controllo che la region non esista già
        if(regionRepository.findByName(name).isPresent()){
            throw new ResourceAlreadyExistsException("Region " + name + " already exists.");
        }

        // Controllo che il country esista
        if(countryRepository.findByName(country).isEmpty()){
            throw new ResourceNotFoundException("Country " + country + " not found.");
        }

        Region region = new Region();
        region.setName(name);
        region.setCountry(country);
        region.setTop10VintagesOfTheMonth(null);
        
        return regionRepository.save(region);
    }


    /// READ operations ///
    // Restituisce la region di nome name
    public Region getRegionByName(String name) {
        Region region = regionRepository.findByName(name)
            .orElseThrow(() -> new ResourceNotFoundException("Region " + name + " not found."));

        return region;
    }

    // Restituisce tutte le regions della collection
    public Page<Region> getAllRegions(Pageable pageable) {
        return regionRepository.findAll(pageable);
    }

    // Restituisce tutte le regions di un determinato country
    public Page<Region> getRegionsByCountry(Pageable pageable, String country) {
        if(countryRepository.findByName(country).isEmpty()){
            throw new ResourceNotFoundException("Country " + country + " not found.");
        }

        Page<Region> regions = regionRepository.findByCountry(pageable, country);

        if(regions.isEmpty()){
            throw new ResourceNotFoundException("No regions found in country " + country + ".");
        }

        return regions;
    }
    
    
    /// UPDATE operations ///
    // Aggiorna il country di una region
    public Region updateRegion(String name, String country_name) {
        return regionRepository.findByName(name)
            .map(region -> {
                // Controllo che il country esista
                Country country = countryRepository.findByName(country_name)
                    .orElseThrow(() -> new ResourceNotFoundException("Country " + country_name + " not found."));

                // Controllo che la region non appartenesse già a quel country
                if(!regionRepository.findByNameAndCountry(name, country_name).isEmpty()){
                    throw new ResourceAlreadyExistsException("Region " + name + " in country " + country_name + " already exists.");
                }
                region.setCountry(country_name);

                // Il country corrispondente alla region deve essere modificato anche nella collection users
                ArrayList<User> users = userRepository.findByAddress_Region(name);
                for(User user : users){
                    user.getAddress().setCountry(country_name);
                    userRepository.save(user);
                }

                // Il country corrispondente alla region deve essere modificato anche nella collection wineries
                ArrayList<Winery> wineries = wineryRepository.findByRegion(name);
                for(Winery winery : wineries){
                    winery.setCountry(country_name);
                    wineryRepository.save(winery);
                }

                // Il country corrispondente alla region deve essere modificato anche nella collection wines
                ArrayList<Wine> wines = wineRepository.findByRegion_Name(name);
                for(Wine wine : wines){
                    wine.getRegion().getCountry().setName(country_name);
                    wine.getRegion().getCountry().setCurrency(country.getCurrency());
                    wineRepository.save(wine);
                }

                return regionRepository.save(region);
            }).orElseThrow(() -> new ResourceNotFoundException("Region " + name + " not found"));
    }
    
    
    /// DELETE operations ///
    // Elimina la region di nome name
    public void deleteRegion(String name) {
        // Controllo che la region esista
        regionRepository.findByName(name)
            .orElseThrow(() -> new ResourceNotFoundException("Region " + name + " not found."));
        
        regionRepository.deleteByName(name);
    }

    // Elimina tutte le regions di un determinato country
    public void deleteRegionsByCountry(String country) {
        // Controllo che il country esista
        countryRepository.findByName(country)
            .orElseThrow(() -> new ResourceNotFoundException("Country " + country + " not found."));
        
        regionRepository.deleteAllByCountry(country);
    }

    // Elimina tutte le regions dalla collection regions
    public void deleteAll() {
        regionRepository.deleteAll();
    }

    //// END of CRUD operations ////
    ////////////////////////////////
}
