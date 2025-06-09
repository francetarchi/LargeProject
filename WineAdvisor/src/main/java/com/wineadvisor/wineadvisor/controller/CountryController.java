package com.wineadvisor.wineadvisor.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;

import com.wineadvisor.wineadvisor.DTO.countries.CreateCountryDTO;
import com.wineadvisor.wineadvisor.DTO.countries.UpdateCountryDTO;
import com.wineadvisor.wineadvisor.service.CountryService;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/countries")
@RequiredArgsConstructor
public class CountryController {
    ////////////////////////////////
    /////////// VARIABLES //////////
    ////////////////////////////////
    private final CountryService countryService;



    ///////////// POST /////////////
    @PostMapping
    public ResponseEntity<?> createCountry(
            @NotNull(message = "New country info cannot be null.") @Valid @RequestBody CreateCountryDTO createCountryDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).header("Location", "/api/countries/" + createCountryDTO.getName()).body(countryService.createCountry(createCountryDTO));
    }
    

    ///////////// GET //////////////
    @GetMapping
    public ResponseEntity<?> getAllCountries(
            @RequestParam(required = false, name = "page number", defaultValue = "0") @PositiveOrZero Integer page) {
        return ResponseEntity.status(HttpStatus.OK).body(countryService.getAllCountries(page));
    }

    @GetMapping("/{name}")
    public ResponseEntity<?> getCountryByName(
            @Parameter(description = "Name of the country to retrieve.", schema = @Schema(type = "string", example = "Italia")) @NotBlank(message = "Name cannot be blank.") @PathVariable String name) {
        return ResponseEntity.status(HttpStatus.OK).body(countryService.getCountryByName(name));
    }
    

    ///////////// PUT //////////////
    @PutMapping("/{name}")
    public ResponseEntity<?> updateCountry(
            @NotBlank(message = "Name cannot be blank.") @PathVariable String name,
            @NotNull(message = "Updated country info cannot be null.") @Valid @RequestBody UpdateCountryDTO updateCountryDTO) {
        return ResponseEntity.status(HttpStatus.OK).body(countryService.updateCountry(name, updateCountryDTO));
    }

    @PutMapping("/{name}/name/update")
    public ResponseEntity<?> updateCountryName(
            @NotBlank(message = "Name cannot be blank.") @PathVariable(name = "name") String targetName,
            @NotBlank(message = "New name cannot be blank.") @RequestParam String newName) {
        return ResponseEntity.status(HttpStatus.OK).body(countryService.updateCountryName(targetName, newName));
    }
    

    //////////// DELETE ////////////
    @DeleteMapping
    public ResponseEntity<?> deleteAllCountries() {
        countryService.deleteAllCountries();
        return ResponseEntity.status(HttpStatus.OK).body("All countries deleted successfully.");
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<?> deleteCountry(
            @NotBlank(message = "Name cannot be blank.") @PathVariable String name) {
        countryService.deleteCountry(name);
        return ResponseEntity.status(HttpStatus.OK).body("Country \"" + name + "\" deleted successfully.");
    }
}
