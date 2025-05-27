package com.wineadvisor.wineadvisor.controller;

import com.wineadvisor.wineadvisor.model.wines.Wine;
import com.wineadvisor.wineadvisor.model.wines.fields.Vintage;
import com.wineadvisor.wineadvisor.service.WineService;
import com.wineadvisor.wineadvisor.exception.BadRequestException;
import com.wineadvisor.wineadvisor.DTO.wines.CreateWineDTO;
import com.wineadvisor.wineadvisor.DTO.wines.NewVintageDTO;
import com.wineadvisor.wineadvisor.DTO.wines.UpdateWineDTO;
import com.wineadvisor.wineadvisor.DTO.wines.UpdateVintageDTO;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/wines")
@RequiredArgsConstructor
public class WineController {
    ////////////////////////////////
    /////////// VARIABLES //////////
    ////////////////////////////////
    private final WineService wineService;



    ////////////// POST //////////////
    @PostMapping
    @Secured( "ROLE_WINERY" )
    public ResponseEntity<?> createWine(
            @NotNull(message = "New wine info cannot be null.") @Valid @RequestBody CreateWineDTO wine) {
        String wineryUsername = (String) SecurityContextHolder.getContext().getAuthentication().getName();
        Wine savedWine = wineService.addWine(wine, wineryUsername);
        return ResponseEntity.status(HttpStatus.CREATED).header("Location", "/api/wines/" + savedWine.getId()).body(savedWine);
    }


    ////////////// GET //////////////
    @GetMapping
    @Secured({ "ROLE_ADMIN" })
    public ResponseEntity<?> getAllWines(
        @RequestParam(required = false, name = "page number", defaultValue = "0") @PositiveOrZero Integer page) {
        Page<Wine> wines = wineService.getAllWines(page);
        return ResponseEntity.status(HttpStatus.OK).body(wines);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getWineById(
            @NotNull(message = "ID cannot be null.") @Positive(message = "ID must be positive.") @PathVariable Long id) {
        Wine wine = wineService.getWineById(id);
        return ResponseEntity.status(HttpStatus.OK).body(wine);
    }

    @GetMapping("/wines/{wineId}/vintages/{vintageYear}")
    public ResponseEntity<?> getVintageById(
            @PathVariable @NotNull(message = "ID cannot be null.") @Positive(message = "ID must be positive.") Long wineId,
            @PathVariable @NotNull(message = "Vintage year cannot be null.") @PositiveOrZero(message = "Vintage year must be positive.") Integer vintageYear) {
        Vintage vintage = wineService.getVintage(wineId, vintageYear);
        return ResponseEntity.status(HttpStatus.OK).body(vintage);
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchWines(
            @RequestParam(required = false, name = "page number", defaultValue = "0")
                @PositiveOrZero Integer page,
            @RequestParam(required = false)
                @Schema(description = "Wine name", example = "Il Pettirosso")
                String name,
            @RequestParam(required = false)
                @Schema(description = "Winery username", example = "arpepe3749")
                String winery,
            @RequestParam(required = false)
                @Schema(description = "Region name", example = "Lombardia")
                String region,
            @RequestParam(required = false)
                @Schema(description = "Country name", example = "Italia")
                String country,
            @RequestParam(required = false)
                @Pattern(regexp = "rosso|bianco|rosato|spumante|vino macerato|vino da dessert|vino liquoroso|vino aromatizzato", message = "Type must be one of: rosso, bianco, rosato, spumante, vino macerato, vino da dessert, vino liquoroso, vino aromatizzato.")
                @Schema(description = "Wine type info", example = "rosso")
                String type,
            @RequestParam(required = false)
                @Schema(description = "Grape name", example = "Nebbiolo")
                String grape,
            @RequestParam(required = false)
                @DecimalMin(value = "0.0", inclusive = true, message = "Price must be at least 0.")
                @Schema(description = "Min vintage price", example = "20.0")
                Double minPrice,
            @RequestParam(required = false)
                @DecimalMin(value = "0.0", inclusive = true, message = "Price must be at least 0.")
                @Schema(description = "Max vintage price", example = "120.0")
                Double maxPrice,
            @RequestParam(required = false)
                @DecimalMin(value = "0.0", inclusive = true, message = "Rating must be at least 0.")
                @DecimalMax(value = "5.0", inclusive = true, message = "Rating must be at most 5.")
                @Schema(description = "Min average wine rating", example = "2.0")
                Double minAverageRating
            ) throws BadRequestException {
                
        name = (name == null) ? "" : name;
        winery = (winery == null) ? "" : winery;
        region = (region == null) ? "" : region;
        country = (country == null) ? "" : country;
        type = (type == null) ? "" : type;
        grape = (grape == null) ? "" : grape;
        minPrice = (minPrice == null) ? 0.0 : minPrice;
        maxPrice = (maxPrice == null) ? 2000.0 : maxPrice;
        minAverageRating = (minAverageRating == null) ? 0.0 : minAverageRating;

        if (minPrice > maxPrice) {
            throw new BadRequestException("Min price cannot be greater than max price.");
        }
        
        return ResponseEntity.status(HttpStatus.OK).body(wineService.searchWines(page, name, winery, region, country, type, grape, minPrice, maxPrice, minAverageRating));
    }

    
    ////////////// PUT //////////////
    @PutMapping
    @Secured({ "ROLE_WINERY" })
    public ResponseEntity<?> updateWine(
                @RequestBody @Valid UpdateWineDTO wine) {
        // Prendo username della winery che vuole modificare il vino
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Wine updatedWine = wineService.updateWine(wine, username);
        return ResponseEntity.status(HttpStatus.OK).body(updatedWine);
    }

    @PutMapping("/vintages/addVintage")
    @Secured({ "ROLE_WINERY" })
    public ResponseEntity<?> addVintage(
                @RequestBody @Valid NewVintageDTO newVintage) {
        // Prendo username della winery che vuole aggiungere la vintage
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Wine savedWine = wineService.addVintage(newVintage, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedWine);
    }

    @PutMapping("/vintages/editVintage")
    @Secured({ "ROLE_WINERY" })
    public ResponseEntity<?> updateVintage(
                @RequestBody @Valid UpdateVintageDTO vintage) {
        // Prendo username della winery che vuole modificare la vintage
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Wine updatedWine = wineService.updateVintage(vintage, username);
        return ResponseEntity.status(HttpStatus.OK).body(updatedWine);
    }

    @PutMapping("/vintages/deleteVintage")
    @Secured({ "ROLE_WINERY" })
    public ResponseEntity<?> deleteVintage(
                @RequestBody @Valid UpdateVintageDTO vintage) {
        // Prendo username della winery che vuole togliere la vintage
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Wine updatedWine = wineService.deleteVintage(vintage, username);
        return ResponseEntity.status(HttpStatus.OK).body(updatedWine);
    }


    ////////////// DELETE //////////////
    @DeleteMapping
    @Secured({ "ROLE_ADMIN" })
    public ResponseEntity<?> deleteAllWines() {
        wineService.deleteAllWines();
        return ResponseEntity.status(HttpStatus.OK).body("All wines deleted successfully.");
    }

    @DeleteMapping("/{id}")
    @Secured({ "ROLE_ADMIN", "ROLE_WINERY" })
    public ResponseEntity<?> deleteWine(
                @PathVariable @NotNull(message = "ID cannot be null.") @Positive(message = "ID cannot be negative.") Long id) {
        // Prendo username della winery che vuole aggiungere la vintage al vino
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        wineService.deleteWineById(id, username);
        return ResponseEntity.status(HttpStatus.OK).body("Wine deleted successfully.");
    }
}
