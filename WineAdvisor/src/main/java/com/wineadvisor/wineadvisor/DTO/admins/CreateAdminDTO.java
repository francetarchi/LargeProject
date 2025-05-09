package com.wineadvisor.wineadvisor.DTO.admins;

import java.time.Instant;

import com.wineadvisor.wineadvisor.DTO.utils.NameDTO;
import com.wineadvisor.wineadvisor.DTO.utils.PasswordDTO;
import com.wineadvisor.wineadvisor.DTO.utils.PictureDTO;
import com.wineadvisor.wineadvisor.model.admin.Admin;
import com.wineadvisor.wineadvisor.model.utils.Login;
import com.wineadvisor.wineadvisor.model.utils.Registered;
import com.wineadvisor.wineadvisor.model.utils.Dob;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;

import lombok.Data;


@Data
@JsonPropertyOrder({ "gender", "name", "email", "telephone", "date of birth", "profile picture", "username", "password" })
public class CreateAdminDTO {
    @Pattern(regexp = "^(male|female|other)$", message = "Gender must be one among \"male\", \"female\" and \"other\" (or blank).")
    @Schema(description = "Gender of the new admin", example = "male")
    private String gender;

    @NotNull(message = "Name info cannot be null.")
    @Valid
    @Schema(name = "name", description = "Name info of the new admin")
    @JsonProperty("name")
    private NameDTO nameDTO;

    @NotBlank(message = "Email cannot be blank.")
    @Email(message = "Email must be a valid email address.")
    @Schema(description = "Email of the new admin", example = "mariorossi@example.com")
    private String email;

    @Pattern(regexp = "^\\+?[0-9\\s\\-()]+$", message = "Telephone must be a valid telephone number.")
    @Schema(description = "Telephone of the new admin", example = "+39 3331234567")
    private String telephone;

    @PastOrPresent(message = "Date must be in the past.")
    // TODO: DA CONTROLLARE SE PUò SERVIRE O NO: @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z$", message = "Date must follow the format 'yyyy-MM-ddTHH:mm:ssZ'")
    @Schema(name = "date of birth", description = "Date of birth of the new admin", example = "1970-01-01T00:00:00.000Z")
    @JsonProperty("date of birth")
    private Instant dob;
    
    @Schema(name = "profile picture", description = "Link of profile picture of the new admin in 3 differents ratios")
    @JsonProperty("profile picture")
    private PictureDTO pictureDTO;
    
    @NotBlank(message = "Username cannot be blank.")
    @Pattern(regexp = "^[a-zA-Z0-9_]{3,50}$", message = "Username must be between 3 and 50 characters long and can contain letters, numbers, and underscores.")
    @Schema(description = "Username of the new admin", example = "user123")
    private String username;

    @NotNull(message = "Password info cannot be null.")
    @Valid
    @Schema(name = "password", description = "Password info of the new admin")
    @JsonProperty("password")
    private PasswordDTO passwordDTO;



    ///////////// METODI PUBBLICI /////////////
    // Ritorna un oggetto di tipo Admin a partire da un oggetto di tipo CreateAdminDTO (inserisce i valori dei soli campi presenti anche nella classe CreateAdminDTO).
    public Admin toAdmin() {
        Admin admin = new Admin(
                null, // _id
                this.gender, // gender
                this.getNameDTO().toName(), // name
                this.email, // email
                this.telephone, // telephone
                new Login(this.username, null), // login
                new Registered(), // registered
                new Dob(), // dob
                this.getPictureDTO().toPicture() // picture
        );
        admin.getDob().setDateTime(this.dob);
        
        return admin;
    }
}
