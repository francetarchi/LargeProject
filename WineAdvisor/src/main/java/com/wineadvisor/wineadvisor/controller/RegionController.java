package com.wineadvisor.wineadvisor.controller;

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

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import lombok.RequiredArgsConstructor;


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
            @Parameter(description = "Name of the new region.", schema = @Schema(example = "Regione")) @NotBlank(message = "Region name cannot be blank.") @RequestParam String name,
            @Parameter(description = "Country of the new region.", schema = @Schema(example = "Italia")) @NotBlank(message = "Country name cannot be blank.") @RequestParam String country) {
        return ResponseEntity.status(HttpStatus.CREATED).header("Location", "/api/regions/" + name).body(regionService.addRegion(name, country));
    }
    

    ////////////// GET //////////////
    @GetMapping
    public ResponseEntity<?> getAllRegions(
            @RequestParam(required = false, name = "page number", defaultValue = "0") @PositiveOrZero Integer page) {
        return ResponseEntity.status(HttpStatus.OK).body(regionService.getAllRegions(page));
    }

    @GetMapping("/{name}")
    public ResponseEntity<?> getRegionByName(
            @Parameter(description = "Name of the region to retrieve.", schema = @Schema(example = "Toscana")) @NotBlank(message = "Region name cannot be blank.") @PathVariable String name) {
        return ResponseEntity.status(HttpStatus.OK).body(regionService.getRegionByName(name));
    }

    @GetMapping("/countries/{country}")
    public ResponseEntity<?> getRegionsByCountry(
            @Parameter(description = "Name of the country to retrieve regions from.", schema = @Schema(example = "Italia")) @NotBlank(message = "Country name cannot be blank.") @PathVariable String country,
            @RequestParam(required = false, name = "page number", defaultValue = "0") @PositiveOrZero Integer page) {
        return ResponseEntity.status(HttpStatus.OK).body(regionService.getRegionsByCountry(page, country));
    }


    ////////////// PUT //////////////
    @PutMapping("/{name}")
    public ResponseEntity<?> updateRegion(
            @Parameter(description = "Name of the region to update.", schema = @Schema(example = "Toscana")) @NotBlank(message = "Region name cannot be blank.") @PathVariable String name,
            @Parameter(description = "Name of the new country to associate with the region.", schema = @Schema(example = "Francia")) @NotBlank(message = "Country name cannot be blank.") @RequestParam String country) {
        return ResponseEntity.status(HttpStatus.OK).body(regionService.updateRegion(name, country));
    }


    ////////////// DELETE //////////////
    @DeleteMapping
    public ResponseEntity<?> deleteAllRegions() {
        regionService.deleteAll();
        return ResponseEntity.status(HttpStatus.OK).body("Regions deleted successfully.");
    }

    @DeleteMapping("/countries/{country}")
    public ResponseEntity<?> deleteRegionsByCountry(
            @Parameter(description = "Name of the country to delete regions from.", schema = @Schema(example = "Italia")) @NotBlank(message = "Country name cannot be blank.") @PathVariable String country) {
        regionService.deleteRegionsByCountry(country);
        return ResponseEntity.status(HttpStatus.OK).body("Regions in country " + country + " deleted successfully.");
    }
    
    @DeleteMapping("/{name}")
    public ResponseEntity<?> deleteRegion(
            @Parameter(description = "Name of the region to delete.", schema = @Schema(example = "Toscana")) @PathVariable @NotBlank(message = "Region name cannot be blank.") String name) {
        regionService.deleteRegion(name);
        return ResponseEntity.status(HttpStatus.OK).body("Region " + name + " deleted successfully.");
    }
}
