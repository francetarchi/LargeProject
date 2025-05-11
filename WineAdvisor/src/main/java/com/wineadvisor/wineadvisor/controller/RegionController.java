package com.wineadvisor.wineadvisor.controller;

import lombok.RequiredArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<?> createRegion(
        @RequestParam @NotBlank(message = "Region name cannot be blank.") String name,
        @RequestParam @NotBlank(message = "Country name cannot be blank.") String country) {
        return ResponseEntity.status(HttpStatus.CREATED).header("Location", "/api/region/" + name).body(regionService.addRegion(name, country));
    }
    
    ////////////// GET //////////////
    @GetMapping("/{name}")
    public ResponseEntity<?> getRegionByName(
        @PathVariable @NotBlank(message = "Region name cannot be blank.") String name) {
        return ResponseEntity.status(HttpStatus.OK).body(regionService.getRegionByName(name));
    }

    @GetMapping
    public ResponseEntity<?> getAllRegions(
        @RequestParam(required = false, name = "page number", defaultValue = "0") @PositiveOrZero Integer page
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(regionService.getAllRegions(page));
    }

    @GetMapping("/countries/{country}")
    public ResponseEntity<?> getRegionsByCountry(
        @RequestParam(required = false, name = "page number", defaultValue = "0") @PositiveOrZero Integer page,
        @PathVariable @NotBlank(message = "Country name cannot be blank.") String country) {
            return ResponseEntity.status(HttpStatus.OK).body(regionService.getRegionsByCountry(page, country));
        }

    ////////////// PUT //////////////
    @PutMapping
    public ResponseEntity<?> updateRegion(
        @RequestParam @NotBlank(message = "Region name cannot be blank.") String name,
        @RequestParam @NotBlank(message = "Country name cannot be blank.") String country) {
        return ResponseEntity.status(HttpStatus.OK).body(regionService.updateRegion(name, country));
    }

    ////////////// DELETE //////////////
    @DeleteMapping("/{name}")
    public ResponseEntity<?> deleteRegion(@PathVariable @NotBlank(message = "Region name cannot be blank.") String name) {
        regionService.deleteRegion(name);
        return ResponseEntity.status(HttpStatus.OK).body("Region " + name + " deleted successfully.");
    }

    @DeleteMapping("/countries/{country}")
    public ResponseEntity<?> deleteRegionsByCountry(@PathVariable @NotBlank(message = "Country name cannot be blank.") String country) {
        regionService.deleteRegionsByCountry(country);
        return ResponseEntity.status(HttpStatus.OK).body("Regions in country " + country + " deleted successfully.");
    }

    @DeleteMapping
    public ResponseEntity<?> deleteAllRegions() {
        regionService.deleteAll();
        return ResponseEntity.status(HttpStatus.OK).body("Regions deleted successfully.");
    }
}
