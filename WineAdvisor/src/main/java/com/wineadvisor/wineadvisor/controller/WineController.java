package com.wineadvisor.wineadvisor.controller;

import com.wineadvisor.wineadvisor.model.Wine;
import com.wineadvisor.wineadvisor.model.fields.wines.Vintage;
import com.wineadvisor.wineadvisor.service.WineService;
import com.wineadvisor.wineadvisor.DTO.WineDTO.*;
import com.wineadvisor.wineadvisor.exception.ResourceNotFoundException;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import lombok.RequiredArgsConstructor;

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

    // CREATE
    @PostMapping
    public ResponseEntity<?> createWine(@RequestBody CreateWineDTO wine, Long wineryId) {
        try {
            // Controllo sul winery id
            if (wineryId == null || wineryId < 0) {
                throw new BadRequestException("Winery ID cannot be null or negative.");
            }
            // Controllo su type che non può essere vuoto
            if (wine.getType() == null || wine.getType().isEmpty()) {
                throw new BadRequestException("Wine type cannot be null or empty.");
            }
            // Controllo su isNatural che non può essere vuoto
            if (wine.getIsNatural() == null) {
                throw new BadRequestException("Wine isNatural cannot be null.");
            }
            // Controllo su name che non può essere vuoto
            if (wine.getName() == null || wine.getName().isEmpty()) {
                throw new BadRequestException("Wine name cannot be null or empty.");
            }


            Wine savedWine = wineService.addWine(wine, wineryId);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedWine);
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()); // 400 Bad Request
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()); // 400 Bad Request
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404 Not Found
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage()); // 500 Internal Server Error
        }
    }

    // READ
    @GetMapping("/{id}")
    public ResponseEntity<?> getWineById(@PathVariable Long id) {
        try {
            // Controllo sull'id che non può essere negativo o null
            if (id == null || id < 0) {
                throw new BadRequestException("Wine ID cannot be null or negative.");
            }

            Wine wine = wineService.getWineById(id);
            return ResponseEntity.status(HttpStatus.OK).body(wine);
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

    @GetMapping("/wine/{wineId}/vintage/{vintageYear}")
    public ResponseEntity<?> getVintageById(@PathVariable Long wineId, @PathVariable Integer vintageYear) {
        try {
            // Controllo sull'id che non può essere negativo o null
            if (wineId == null || wineId < 0) {
                throw new BadRequestException("Wine ID cannot be null or negative.");
            }
            if (vintageYear == null || vintageYear < 0) {
                throw new BadRequestException("Vintage ID cannot be null or negative.");
            }

            Vintage vintage = wineService.getVintage(wineId, vintageYear);
            return ResponseEntity.status(HttpStatus.OK).body(vintage);
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
    public ResponseEntity<?> searchWinesByName(Pageable pageable, @RequestParam String keyword) {
        try {
            if (keyword == null || keyword.isEmpty()) {
                throw new BadRequestException("Keyword cannot be null or empty.");
            }
            Page<Wine> wines = wineService.getWinesByName(pageable, keyword);
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

    @GetMapping("/winery/{wineryId}")
    public ResponseEntity<?> getWinesByWinery(Pageable pageable, @PathVariable Long wineryId) {
        try {
            if (wineryId == null || wineryId < 0) {
                throw new BadRequestException("Winery ID cannot be null or negative.");
            }
            Page<Wine> wines = wineService.getWinesByWinery(pageable, wineryId);
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

    @GetMapping("/region/{region}")
    public ResponseEntity<?> getWinesByRegion(Pageable pageable, @PathVariable String region) {
        try {
            if (region == null || region.isEmpty()) {
                throw new BadRequestException("Region cannot be null or empty.");
            }
            Page<Wine> wines = wineService.getWinesByRegion(pageable, region);
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

    @GetMapping("/country/{country}")
    public ResponseEntity<?> getWinesByCountry(Pageable pageable, @PathVariable String country) {
        try {
            if (country == null || country.isEmpty()) {
                throw new BadRequestException("Country cannot be null or empty.");
            }
            Page<Wine> wines = wineService.getWinesByCountry(pageable, country);
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

    @GetMapping("/type/{type}")
    public ResponseEntity<?> getWinesByType(Pageable pageable, @PathVariable String type) {
        try {
            if (type == null || type.isEmpty()) {
                throw new BadRequestException("Type cannot be null or empty.");
            }
            Page<Wine> wines = wineService.getWinesByType(pageable, type);
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

    @GetMapping("/grape/{grape}")
    public ResponseEntity<?> getWinesByGrape(Pageable pageable, @PathVariable String grape) {
        try {
            if (grape == null || grape.isEmpty()) {
                throw new BadRequestException("Grape cannot be null or empty.");
            }
            Page<Wine> wines = wineService.getWinesByGrapeName(pageable, grape);
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

    @GetMapping("/price")
    public ResponseEntity<?> getWinesByPrice(Pageable pageable, @RequestParam Double minPrice, @RequestParam Double maxPrice) {
        try {
            if (minPrice == null || maxPrice == null) {
                throw new BadRequestException("Min and Max price cannot be null.");
            }
            if (minPrice < 0 || maxPrice < 0) {
                throw new BadRequestException("Min and Max price cannot be negative.");
            }
            if (minPrice > maxPrice) {
                throw new BadRequestException("Min price cannot be greater than Max price.");
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
    public ResponseEntity<?> getWinesByRating(Pageable pageable, @RequestParam Double minRating) {
        try {
            if (minRating == null) {
                throw new BadRequestException("Min rating cannot be null.");
            }
            if (minRating < 0) {
                throw new BadRequestException("Min rating cannot be negative.");
            }
            Page<Wine> wines = wineService.getWinesByMinAverageRating(pageable, minRating);
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

    // UPDATE
    @PutMapping("/add-vintage")
    public ResponseEntity<?> addVintage(@RequestBody NewVintageDTO newVintage){
        try {
            // Controllo sull'id
            if (newVintage.getWineId() == null || newVintage.getWineId() < 0) {
                throw new BadRequestException("Wine ID cannot be null or negative.");
            }
            // Controllo su year
            if (newVintage.getYear() == null || newVintage.getYear() < 0) {
                throw new BadRequestException("Vintage year cannot be null or negative.");
            }
            // Controllo su price
            if (newVintage.getPrice() == null || newVintage.getPrice() < 0) {
                throw new BadRequestException("Price cannot be null or negative.");
            }

            Wine savedWine = wineService.addVintage(newVintage);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedWine);
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

    @PutMapping("/edit-wine")
    public ResponseEntity<?> updateWine(@RequestBody UpdateWineDTO wine){
        try {
            // Controllo su id
            if (wine.getWineId() == null || wine.getWineId() < 0) {
                throw new BadRequestException("Wine ID cannot be null or negative.");
            }
            // Controllo su name
            if (wine.getName() == null || wine.getName().isEmpty()) {
                throw new BadRequestException("Wine name cannot be null or empty.");
            }
            // Controllo su type
            if (wine.getType() == null || wine.getType().isEmpty()) {
                throw new BadRequestException("Wine type cannot be null or empty.");
            }
            // Controllo su isNatural
            if (wine.getIsNatural() == null) {
                throw new BadRequestException("Wine isNatural cannot be null.");
            }

            Wine updatedWine = wineService.updateWine(wine);
            return ResponseEntity.status(HttpStatus.OK).body(updatedWine);
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

    @PutMapping("/edit-vintage")
    public ResponseEntity<?> updateVintage(@RequestBody UpdateVintageDTO vintage){
        try {
            // Controllo su id
            if (vintage.getWineId() == null || vintage.getWineId() < 0) {
                throw new BadRequestException("Wine ID cannot be null or negative.");
            }
            // Controllo su year
            if (vintage.getYear() == null || vintage.getYear() < 0) {
                throw new BadRequestException("Vintage year cannot be null or negative.");
            }
            // Controllo su price
            if (vintage.getPrice() == null || vintage.getPrice() < 0) {
                throw new BadRequestException("Price cannot be null or negative.");
            }

            Wine updatedWine = wineService.updateVintage(vintage);
            return ResponseEntity.status(HttpStatus.OK).body(updatedWine);
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

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteWine(@PathVariable Long id) {
        try {
            // Controllo sull'id
            if (id == null || id < 0) {
                throw new BadRequestException("Wine ID cannot be null or negative.");
            }

            wineService.deleteWineById(id);
            return ResponseEntity.status(HttpStatus.OK).body("Wine deleted successfully.");
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

    @DeleteMapping("/{wineId}/vintage/{vintageYear}")
    public ResponseEntity<?> deleteVintage(@PathVariable Long wineId, @PathVariable Integer vintageYear) {
        try {
            // Controllo sull'id
            if (wineId == null || wineId < 0) {
                throw new BadRequestException("Wine ID cannot be null or negative.");
            }
            if (vintageYear == null || vintageYear < 0) {
                throw new BadRequestException("Vintage year cannot be null or negative.");
            }

            wineService.deleteVintage(wineId, vintageYear);
            return ResponseEntity.status(HttpStatus.OK).body("Vintage deleted successfully.");
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
