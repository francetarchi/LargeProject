package com.wineadvisor.wineadvisor.controller;

import lombok.RequiredArgsConstructor;

import jakarta.validation.constraints.NotBlank;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
// import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Pageable;

import com.wineadvisor.wineadvisor.service.RegionService;

@RestController
@RequestMapping("/api/regions")
@RequiredArgsConstructor
public class RegionController {
    ////////////////////////////////
    /////////// VARIABLES //////////
    ////////////////////////////////
    private final RegionService regionService;

    ////////////// POST //////////////
    @PostMapping
    // @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> createRegion(
        @RequestParam @NotBlank(message = "Region name cannot be blank.") String name,
        @RequestParam @NotBlank(message = "Country name cannot be blank.") String country) {
        return ResponseEntity.status(HttpStatus.CREATED).body(regionService.addRegion(name, country));
    }
    
    ////////////// GET //////////////
    @GetMapping("/{name}")
    // @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getRegionByName(
        @PathVariable @NotBlank(message = "Region name cannot be blank.") String name) {
        return ResponseEntity.status(HttpStatus.OK).body(regionService.getRegionByName(name));
    }

    @GetMapping
    // @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getAllRegions(Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(regionService.getAllRegions(pageable));
    }

    @GetMapping("/countries/{country}")
    // @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getRegionsByCountry(
        Pageable pageable,
        @PathVariable @NotBlank(message = "Country name cannot be blank.") String country) {
            return ResponseEntity.status(HttpStatus.OK).body(regionService.getRegionsByCountry(pageable, country));
        }

    ////////////// PUT //////////////
    @PutMapping
    // @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> updateRegion(
        @RequestParam @NotBlank(message = "Region name cannot be blank.") String name,
        @RequestParam @NotBlank(message = "Country name cannot be blank.") String country) {
        return ResponseEntity.status(HttpStatus.OK).body(regionService.updateRegion(name, country));
    }

    ////////////// DELETE //////////////
    @DeleteMapping("/{name}")
    // @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> deleteRegion(@PathVariable @NotBlank(message = "Region name cannot be blank.") String name) {
        regionService.deleteRegion(name);
        return ResponseEntity.status(HttpStatus.OK).body("Region " + name + " deleted successfully.");
    }

    @DeleteMapping("/countries/{country}")
    // @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> deleteRegionsByCountry(@PathVariable @NotBlank(message = "Country name cannot be blank.") String country) {
        regionService.deleteRegionsByCountry(country);
        return ResponseEntity.status(HttpStatus.OK).body("Regions in country " + country + " deleted successfully.");
    }

    @DeleteMapping
    // @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> deleteAllRegions() {
        regionService.deleteAll();
        return ResponseEntity.status(HttpStatus.OK).body("Regions deleted successfully.");
    }
}
