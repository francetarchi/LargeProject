package com.wineadvisor.wineadvisor.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
/* TODO: Uncomment the following and delete the previous line if you want to add admin authentication */
// import org.springframework.security.access.annotation.Secured;
// import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wineadvisor.wineadvisor.service.AnalyticsService;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {
    ////////////////////////////////
    /////////// VARIABLES //////////
    ////////////////////////////////
    private final AnalyticsService analyticsService;

    /////////// COSTANTI ////////////
    /* TODO: Uncomment the following and delete the previous line if you want to add admin authentication */
    // private final String TOP_VINTAGES_OUR_QOP = "top_vintages_by_our_qop_per_type";
    // private final String TOP_VINTAGES_QOP = "top_vintages_by_qop_per_type";
    // private final String TOP_VINTAGES_RATINGS = "top_vintages_by_ratings_per_type";
    // private final String TOP_WINES_RATINGS = "top_wines_by_ratings_per_type";
    // private final String TOP_WINERIES_RATINGS = "top_wineries_by_wines_ratings";



    ////////////// GET //////////////
    @GetMapping("/top-vintages-our-qop-type/{type}")
    public ResponseEntity<?> getTopVintagesByOurQopPerType(
            @NotBlank(message = "Type cannot be blank.") @PathVariable String type) {
        return ResponseEntity.status(HttpStatus.OK).body(analyticsService.getTopVintagesByOurQopPerType(type));
    }

    @GetMapping("/top-vintages-qop-type/{type}")
    public ResponseEntity<?> getTopVintagesByQopPerType(
            @NotBlank(message = "Type cannot be blank.") @PathVariable String type) {
        return ResponseEntity.status(HttpStatus.OK).body(analyticsService.getTopVintagesByQopPerType(type));
    }

    @GetMapping("/top-vintages-ratings-type/{type}")
    public ResponseEntity<?> getTopVintagesByRatingsPerType(
            @NotBlank(message = "Type cannot be blank.") @PathVariable String type) {
        return ResponseEntity.status(HttpStatus.OK).body(analyticsService.getTopVintagesByRatingsPerType(type));
    }

    @GetMapping("/top-wines-ratings-type/{type}")
    public ResponseEntity<?> getTopWinesByRatingsPerType(
            @NotBlank(message = "Type cannot be blank.") @PathVariable String type) {
        return ResponseEntity.status(HttpStatus.OK).body(analyticsService.getTopWinesByRatingsPerType(type));
    }

    @GetMapping("/top-wineries-ratings")
    public ResponseEntity<?> getTopWineriesByWinesRatings() {
        return ResponseEntity.status(HttpStatus.OK).body(analyticsService.getTopWineriesByWinesRatings());
    }

    @GetMapping("/top-10-vintages")
    public ResponseEntity<?> getTop10VintagesOfTheMonth() {
        String username = ((org.springframework.security.core.userdetails.User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        return ResponseEntity.status(HttpStatus.OK).body(analyticsService.getTop10VintagesOfTheMonth(username));
    }

    @GetMapping("/top-100-vintages")
    public ResponseEntity<?> getTop100VintagesOfTheMonth(
        @RequestParam(required = false, name = "page number", defaultValue = "0") @PositiveOrZero Integer page
        ) {
        String username = ((org.springframework.security.core.userdetails.User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        return ResponseEntity.status(HttpStatus.OK).body(analyticsService.getTop100VintagesOfTheMonth(username, page));
    }
    
    ///////////// DELETE ////////////
    /* TODO: Uncomment the following and delete the previous line if you want to add admin authentication */
    // @Secured("ROLE_ADMIN")
    // @DeleteMapping("/top-vintages-our-qop-type")
    // public ResponseEntity<?> deleteTopVintagesByOurQopPerType() {
    //     analyticsService.deleteTopVintagesByOurQopPerType();
    //     return ResponseEntity.status(HttpStatus.OK).body("All documents in ranking \"" +  TOP_VINTAGES_OUR_QOP + "\" deleted successfully.");
    // }

    // @Secured("ROLE_ADMIN")
    // @DeleteMapping("/top-vintages-our-qop-type/{type}")
    // public ResponseEntity<?> deleteTopVintagesByOurQopPerTypeByType(
    //         @NotBlank(message = "Type cannot be blank.") @PathVariable String type) {
    //     analyticsService.deleteTopVintagesByOurQopPerTypeByType(type);
    //     return ResponseEntity.status(HttpStatus.OK).body("Document with type \"" +  type + "\" in ranking \"" +  TOP_VINTAGES_OUR_QOP + "\" deleted successfully.");
    // }


    // @Secured("ROLE_ADMIN")
    // @DeleteMapping("/top-vintages-qop-type")
    // public ResponseEntity<?> deleteTopVintagesByQopPerType() {
    //     analyticsService.deleteTopVintagesByQopPerType();
    //     return ResponseEntity.status(HttpStatus.OK).body("All documents in ranking \"" +  TOP_VINTAGES_QOP + "\" deleted successfully.");
    // }

    // @Secured("ROLE_ADMIN")
    // @DeleteMapping("/top-vintages-qop-type/{type}")
    // public ResponseEntity<?> deleteTopVintagesByQopPerTypeByType(
    //         @NotBlank(message = "Type cannot be blank.") @PathVariable String type) {
    //     analyticsService.deleteTopVintagesByQopPerTypeByType(type);
    //     return ResponseEntity.status(HttpStatus.OK).body("Document with type \"" +  type + "\" in ranking \"" +  TOP_VINTAGES_QOP + "\" deleted successfully.");
    // }


    // @Secured("ROLE_ADMIN")
    // @DeleteMapping("/top-vintages-ratings-type")
    // public ResponseEntity<?> deleteTopVintagesByRatingsPerType() {
    //     analyticsService.deleteTopVintagesByRatingsPerType();
    //     return ResponseEntity.status(HttpStatus.OK).body("All documents in ranking \"" +  TOP_VINTAGES_RATINGS + "\" deleted successfully.");
    // }

    // @Secured("ROLE_ADMIN")
    // @DeleteMapping("/top-vintages-ratings-type/{type}")
    // public ResponseEntity<?> deleteTopVintagesByRatingsPerTypeByType(
    //         @NotBlank(message = "Type cannot be blank.") @PathVariable String type) {
    //     analyticsService.deleteTopVintagesByRatingsPerTypeByType(type);
    //     return ResponseEntity.status(HttpStatus.OK).body("Document with type \"" +  type + "\" in ranking \"" +  TOP_VINTAGES_RATINGS + "\" deleted successfully.");
    // }


    // @Secured("ROLE_ADMIN")
    // @DeleteMapping("/top-wines-ratings-type")
    // public ResponseEntity<?> deleteTopWinesByRatingsPerType() {
    //     analyticsService.deleteTopWinesByRatingsPerType();
    //     return ResponseEntity.status(HttpStatus.OK).body("All documents in ranking \"" +  TOP_WINES_RATINGS + "\" deleted successfully.");
    // }

    // @Secured("ROLE_ADMIN")
    // @DeleteMapping("/top-wines-ratings-type/{type}")
    // public ResponseEntity<?> deleteTopWinesByRatingsPerTypeByType(
    //         @NotBlank(message = "Type cannot be blank.") @PathVariable String type) {
    //     analyticsService.deleteTopWinesByRatingsPerTypeByType(type);
    //     return ResponseEntity.status(HttpStatus.OK).body("Document with type \"" +  type + "\" in ranking \"" +  TOP_WINES_RATINGS + "\" deleted successfully.");
    // }


    // @Secured("ROLE_ADMIN")
    // @DeleteMapping("/top-wineries-ratings")
    // public ResponseEntity<?> deleteTopWineriesByWinesRatings() {
    //     analyticsService.deleteTopWineriesByWinesRatings();
    //     return ResponseEntity.status(HttpStatus.OK).body("All documents in ranking \"" +  TOP_WINERIES_RATINGS + "\" deleted successfully.");
    // }

    // @Secured("ROLE_ADMIN")
    // @DeleteMapping("/top-wineries-ratings/{winery_username}")
    // public ResponseEntity<?> deleteTopWineriesByWinesRatingsByWineryUsername(
    //         @NotBlank(message = "Winery username cannot be blank.") @PathVariable String winery_username) {
    //     analyticsService.deleteTopWineriesByWinesRatingsByWineryUsername(winery_username);
    //     return ResponseEntity.status(HttpStatus.OK).body("Document with winery_username \"" +  winery_username + "\" in ranking \"" +  TOP_WINERIES_RATINGS + "\" deleted successfully.");
    // }
}
