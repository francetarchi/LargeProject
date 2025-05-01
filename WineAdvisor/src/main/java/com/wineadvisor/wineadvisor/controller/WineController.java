package com.wineadvisor.wineadvisor.controller;

import com.wineadvisor.wineadvisor.model.wines.Wine;
import com.wineadvisor.wineadvisor.model.wines.fields.Vintage;
import com.wineadvisor.wineadvisor.service.WineService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import com.wineadvisor.wineadvisor.DTO.wines.*;
import com.wineadvisor.wineadvisor.exception.ResourceNotFoundException;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;

import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.security.core.context.SecurityContextHolder;


@RestController
@RequestMapping("/wines")
@RequiredArgsConstructor
public class WineController {
    private final WineService wineService;

    ////////////// POST //////////////
    @PostMapping
    public ResponseEntity<?> createWine(
            @NotNull(message = "New wine info cannot be null.") @Valid @RequestBody CreateWineDTO wine,
            @NotBlank(message = "Winery ID cannot be blank.") @RequestParam(required = true, name = "winery") String wineryUsername) {
        try {
            Wine savedWine = wineService.addWine(wine, wineryUsername);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedWine);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()); // 400 Bad Request
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404 Not Found
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage()); // 500 Internal Server Error
        }
    }

    ////////////// GET //////////////
    @GetMapping("/{id}")
    public ResponseEntity<?> getWineById(
            @PathVariable @NotNull(message = "ID cannot be null.") @Positive(message = "ID must be positive.") Long id) {
        try {
            Wine wine = wineService.getWineById(id);
            return ResponseEntity.status(HttpStatus.OK).body(wine);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/wine/{wineId}/vintage/{vintageYear}")
    public ResponseEntity<?> getVintageById(
            @PathVariable @NotNull(message = "ID cannot be null.") @Positive(message = "ID must be positive.") Long wineId,
            @PathVariable @NotNull(message = "Vintage year cannot be null.") @PositiveOrZero(message = "Vintage year must be positive.") Integer vintageYear) {
        try {
            Vintage vintage = wineService.getVintage(wineId, vintageYear);
            return ResponseEntity.status(HttpStatus.OK).body(vintage);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllWines(Pageable pageable) {
        try {
            Page<Wine> wines = wineService.getAllWines(pageable);
            return ResponseEntity.status(HttpStatus.OK).body(wines);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchWinesByName(
            Pageable pageable,
            @RequestParam @NotBlank(message = "Keyword cannot be blank.") String keyword) {
        try {
            Page<Wine> wines = wineService.getWinesByName(pageable, keyword);
            return ResponseEntity.status(HttpStatus.OK).body(wines);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/winery/{wineryId}")
    public ResponseEntity<?> getWinesByWinery(
            Pageable pageable,
            @PathVariable @NotBlank(message = "Winery username cannot be null.") String wineryUsername) {
        try {
            Page<Wine> wines = wineService.getWinesByWinery(pageable, wineryUsername);
            return ResponseEntity.status(HttpStatus.OK).body(wines);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/region/{region}")
    public ResponseEntity<?> getWinesByRegion(
            Pageable pageable,
            @PathVariable @NotBlank(message = "Region cannot be blank.") String region) {
        try {
            Page<Wine> wines = wineService.getWinesByRegion(pageable, region);
            return ResponseEntity.status(HttpStatus.OK).body(wines);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/country/{country}")
    public ResponseEntity<?> getWinesByCountry(
            Pageable pageable,
            @PathVariable @NotBlank(message = "Country cannot be blank.") String country) {
        try {
            Page<Wine> wines = wineService.getWinesByCountry(pageable, country);
            return ResponseEntity.status(HttpStatus.OK).body(wines);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<?> getWinesByType(
            Pageable pageable,
            @PathVariable @NotBlank(message = "Type cannot be blank.") String type) {
        try {
            Page<Wine> wines = wineService.getWinesByType(pageable, type);
            return ResponseEntity.status(HttpStatus.OK).body(wines);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/grape/{grape}")
    public ResponseEntity<?> getWinesByGrape(
            Pageable pageable,
            @PathVariable @NotBlank(message = "Grape name cannot be blank.") String grape) {
        try {
            Page<Wine> wines = wineService.getWinesByGrapeName(pageable, grape);
            return ResponseEntity.status(HttpStatus.OK).body(wines);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/price")
    public ResponseEntity<?> getWinesByPrice(
            Pageable pageable,
            @RequestParam @NotNull(message = "Price cannot be null.") @Positive(message = "Price cannot be negative.") Double minPrice,
            @RequestParam @NotNull(message = "Price cannot be null.") @Positive(message = "Price cannot be negative.") Double maxPrice) {
        try {
            if (minPrice >= maxPrice) {
                throw new BadRequestException("Min price must be less than max price.");
            }

            Page<Wine> wines = wineService.getWinesByPriceRange(pageable, minPrice, maxPrice);
            return ResponseEntity.status(HttpStatus.OK).body(wines);
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/rating")
    public ResponseEntity<?> getWinesByRating(
            Pageable pageable,
            @RequestParam
                @NotNull(message = "Rating cannot be null.")
                @PositiveOrZero(message = "Rating cannot be negative.")
                @DecimalMin(value = "0.0", inclusive = true, message = "Rating must be at least 0.")
                @DecimalMax(value = "5.0", inclusive = true, message = "Rating must be at most 5.")
                Double minRating) {
        try {
            Page<Wine> wines = wineService.getWinesByMinAverageRating(pageable, minRating);
            return ResponseEntity.status(HttpStatus.OK).body(wines);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    // ANALYTICS
    @GetMapping("/analytics/top-10-wines")
    public ResponseEntity<?> getTop10Wines() {
        try {
            ArrayList<Wine> wines = wineService.getTop10Wines();
            return ResponseEntity.status(HttpStatus.OK).body(wines);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/analytics/most-popular-in-your-region/{username}")
    public ResponseEntity<?> getMostPopularWinesInUserRegion(
            @PathVariable @NotBlank(message = "Username cannot be blank.") String username){
        try {
            ArrayList<Wine> wines = wineService.getMostPopularWinesInUserRegion(username);
            return ResponseEntity.status(HttpStatus.OK).body(wines);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/analytics/vintages-recommendations/{username}")
    public ResponseEntity<?> getRecommendedVintages(
            @PathVariable @NotBlank(message = "Username cannot be blank.") String username){
        try {
            ArrayList<Vintage> vintages = wineService.getRecommendedVintages(username);
            return ResponseEntity.status(HttpStatus.OK).body(vintages);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    ////////////// PUT //////////////
    @PutMapping("/add-vintage")
    public ResponseEntity<?> addVintage(@RequestBody @Valid NewVintageDTO newVintage){
        try {
            Wine savedWine = wineService.addVintage(newVintage);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedWine);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/edit-wine")
    public ResponseEntity<?> updateWine(@RequestBody @Valid UpdateWineDTO wine){
        try {
            Wine updatedWine = wineService.updateWine(wine);
            return ResponseEntity.status(HttpStatus.OK).body(updatedWine);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/edit-vintage")
    public ResponseEntity<?> updateVintage(@RequestBody @Valid UpdateVintageDTO vintage){
        try {
            Wine updatedWine = wineService.updateVintage(vintage);
            return ResponseEntity.status(HttpStatus.OK).body(updatedWine);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    ////////////// DELETE //////////////
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteWine(
            @PathVariable @NotNull(message = "ID cannot be null.") @Positive(message = "ID cannot be negative.") Long id) {
        try {
            wineService.deleteWineById(id);
            return ResponseEntity.status(HttpStatus.OK).body("Wine deleted successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/{wineId}/vintage/{vintageYear}")
    public ResponseEntity<?> deleteVintage(
            @PathVariable @NotNull(message = "ID cannot be null.") @Positive(message = "ID cannot be negative.") Long wineId,
            @PathVariable @NotNull(message = "Vintage year cannot be null.") @Positive(message = "Vintage year cannot be negative.") Integer vintageYear) {
        try {
            wineService.deleteVintage(wineId, vintageYear);
            return ResponseEntity.status(HttpStatus.OK).body("Vintage deleted successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/delete-all")
    public ResponseEntity<?> deleteAllWines() {
        try {
            wineService.deleteAllWines();
            return ResponseEntity.status(HttpStatus.OK).body("All wines deleted successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }    
}
