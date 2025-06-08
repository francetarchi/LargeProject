package com.wineadvisor.wineadvisor.controller;

import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import com.wineadvisor.wineadvisor.DTO.styles.StyleDTO;
import com.wineadvisor.wineadvisor.service.StyleService;


@RestController
@RequestMapping("/api/styles")
@RequiredArgsConstructor
public class StyleController {
    ////////////////////////////////
    /////////// VARIABLES //////////
    ////////////////////////////////
    private final StyleService styleService;


    ////////////// POST //////////////
    @PostMapping
    @Secured({ "ROLE_ADMIN", "ROLE_WINERY" })
    public ResponseEntity<?> createStyle(@Valid @RequestBody StyleDTO style) {
        return ResponseEntity.status(HttpStatus.CREATED).header("Location", "/api/styles/" + style.getName()).body(styleService.createStyle(style));
    }

    ////////////// GET //////////////
    @GetMapping
    public ResponseEntity<?> getAllStyles(
        @RequestParam(required = false, name = "page number", defaultValue = "0") @PositiveOrZero Integer page) {
        return ResponseEntity.status(HttpStatus.OK).body(styleService.getAllStyles(page));
    }

    @GetMapping("/{name}")
    public ResponseEntity<?> getStyleByName(
                @PathVariable @NotBlank(message = "Style name cannot be blank.") String name) {
        return ResponseEntity.status(HttpStatus.OK).body(styleService.getStyleByName(name));
    }

    ////////////// PUT //////////////
    @PutMapping
    @Secured({ "ROLE_ADMIN" })
    public ResponseEntity<?> updateStyle(@Valid @RequestBody StyleDTO style) {
        System.out.println("body: " + style);
        return ResponseEntity.status(HttpStatus.OK).body(styleService.updateStyle(style));
    }

    ////////////// DELETE //////////////
    @DeleteMapping
    @Secured({ "ROLE_ADMIN" })
    public ResponseEntity<?> deleteAllStyles() {
        styleService.deleteAll();
        return ResponseEntity.status(HttpStatus.OK).body("Styles deleted successfully.");
    }

    @DeleteMapping("/{name}")
    @Secured({ "ROLE_ADMIN" })
    public ResponseEntity<?> deleteStyle(
                @PathVariable @NotBlank(message = "Style name cannot be blank.") String name) {
        styleService.deleteStyleByName(name);
        return ResponseEntity.status(HttpStatus.OK).body("Style " + name + " deleted successfully.");
    }
}
