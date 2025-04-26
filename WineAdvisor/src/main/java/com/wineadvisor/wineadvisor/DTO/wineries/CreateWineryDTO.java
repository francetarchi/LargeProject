package com.wineadvisor.wineadvisor.DTO.wineries;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.wineadvisor.wineadvisor.DTO.utils.PasswordDTO;
import com.wineadvisor.wineadvisor.DTO.utils.PictureDTO;
import com.wineadvisor.wineadvisor.model.utils.Login;
import com.wineadvisor.wineadvisor.model.utils.Registered;
import com.wineadvisor.wineadvisor.model.wineries.Winery;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@JsonPropertyOrder({ "winery name", "address", "city", "zipcode", "province", "region", "country", "email", "telephone", "website", "facebook", "instagram", "profile picture", "username", "password" })
public class CreateWineryDTO {
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
    
    @NotBlank(message = "Username cannot be blank.")
    @Pattern(regexp = "^[a-zA-Z0-9_]{3,50}$", message = "Username must be between 3 and 50 characters long and can contain letters, numbers, and underscores.")
    @Schema(description = "Username of the new winery", example = "user123")
    private String username;

    @NotNull(message = "Password info cannot be null.")
    @Valid
    @Schema(name = "password", description = "Password info of the new winery", example = "{ \"oldPassword\": \"oldPass123!\", \"newPassword\": \"newPass123!\", \"confirmPassword\": \"newPass123!\" }")
    @JsonProperty("password")
    private PasswordDTO passwordDTO;



    ///////////// METODI PUBBLICI /////////////
    // Ritorna un oggetto di tipo Winery a partire dall'oggetto attuale (this.) di tipo CreateWineryDTO (inserisce i valori dei soli campi presenti anche nella classe CreateWineryDTO).
    public Winery toWinery() {
        Winery winery = new Winery(
                null, // _id
                this.name, // name
                this.address, // address
                this.city, // city
                this.zipcode, // zipcode
                this.province, // province
                this.region, // region
                this.country, // country
                this.telephone, // telephone
                this.email, // email
                this.website, // website
                this.facebook, // facebook
                this.instagram, // instagram
                new Login(this.username, null), // login
                new Registered(), // registered
                this.getPictureDTO().toPicture(), // picture
                new ArrayList<>() // images
        );
        
        return winery;
    }
}
