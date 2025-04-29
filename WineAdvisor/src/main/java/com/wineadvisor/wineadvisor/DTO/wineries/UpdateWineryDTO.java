package com.wineadvisor.wineadvisor.DTO.wineries;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.wineadvisor.wineadvisor.DTO.utils.PictureDTO;
import com.wineadvisor.wineadvisor.model.wineries.Winery;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@JsonPropertyOrder({ "winery name", "address", "city", "zipcode", "province", "region", "country", "email", "telephone", "website", "facebook", "instagram", "profile picture" })
public class UpdateWineryDTO {
    @NotBlank(message = "Name cannot be blank.")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ'\\-\\s]+$", message = "Name can only contain letters, spaces, hyphens, and apostrophes.")
    @Schema(name = "winery name", description = "Name of the new winery", example = "Azienda Viticola Mario Rossi")
    @JsonProperty("winery name")
    private String name;
    
    @NotBlank(message = "Address cannot be blank.")
    @Pattern(regexp = "^[a-zA-Z0-9À-ÿ'\\-\\s]+$", message = "Address can only contain letters, numbers, spaces, hyphens, and apostrophes.")
    @Schema(description = "Address of the new winery", example = "Via Roma, 1a")
    private String address;

    @NotBlank(message = "City cannot be blank.")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ'\\-\\s]+$", message = "City can only contain letters, spaces, apostrophes, and hyphens.")
    @Schema(description = "City of the address", example = "Pisa")
    private String city;
    
    @NotBlank(message = "Zipcode cannot be blank.")
    @Pattern(regexp = "^[0-9]{4,10}$", message = "Zipcode must be between 4 and 10 digits.")
    @Schema(description = "Zipcode of the address", example = "56126")
    private String zipcode;
    
    @NotBlank(message = "Province cannot be blank.")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ'\\-\\s]+$", message = "Province can only contain letters, spaces, apostrophes, and hyphens.")
    @Schema(description = "Province of the address", example = "Pisa")
    private String province;
    
    @NotBlank(message = "Region cannot be blank.")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ'\\-\\s]+$", message = "Region can only contain letters, spaces, apostrophes, and hyphens.")
    @Schema(description = "Region of the address", example = "Toscana")
    private String region;
    
    @NotBlank(message = "Country cannot be blank.")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ'\\-\\s]+$", message = "Country can only contain letters, spaces, apostrophes, and hyphens.")
    @Schema(description = "Country of the address", example = "Italia")
    private String country;

    @NotBlank(message = "Email cannot be blank.")
    @Email(message = "Email must be a valid email address.")
    @Schema(description = "Email of the new winey", example = "aziendaViticolaMarioRossi@example.com")
    private String email;
    
    @Pattern(regexp = "^\\+?[0-9\\s\\-()]+$", message = "Telephone must be a valid telephone number.")
    @Schema(description = "Telephone of the new winery", example = "+39 3331234567")
    private String telephone;

    @Pattern(regexp = "^(http|https)://.*$", message = "Website must be a valid URL.")
    @Schema(description = "Link to the website of the new winery", example = "http://www.aziendaViticolaMarioRossi.com")
    private String website;
    
    @Pattern(regexp = "^(http|https)://.*$", message = "Facebook link must be a valid URL.")
    @Schema(description = "Link to the Facebook profile of the new winery", example = "http://www.facebook.com/aziendaViticolaMarioRossi")
    private String facebook;
    
    @Pattern(regexp = "^(http|https)://.*$", message = "Instagram link must be a valid URL.")
    @Schema(description = "Link to the Instagram profile of the new winery", example = "http://www.instagram.com/aziendaViticolaMarioRossi")
    private String instagram;
    
    @Schema(name = "profile picture", description = "Link to the profile picture of the new winery in 3 differents ratios")
    @JsonProperty("profile picture")
    private PictureDTO pictureDTO;



    ///////////// METODI PUBBLICI /////////////
    // Modifica l'oggetto di classe Winery passato come argomento sostituendo i campi aggiornabili con i valori aggiornati (quelli dell'istanza attuale (this.)). Ritorna la winery aggiornata.
    public Winery toWinery(Winery targetWinery) {
        targetWinery.setName(this.name);
        targetWinery.setAddress(this.address);
        targetWinery.setCity(this.city);
        targetWinery.setZipcode(this.zipcode);
        targetWinery.setProvince(this.province);
        targetWinery.setRegion(this.region);
        targetWinery.setCountry(this.country);
        targetWinery.setEmail(this.email);
        targetWinery.setTelephone(this.telephone);
        targetWinery.setWebsite(this.website);
        targetWinery.setFacebook(this.facebook);
        targetWinery.setInstagram(this.instagram);
        targetWinery.setPicture(this.getPictureDTO().toPicture());

        return targetWinery;
    }
}
