package com.wineadvisor.wineadvisor.DTO.users;

import java.time.LocalDateTime;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.wineadvisor.wineadvisor.DTO.users.fields.AddressDTO;
import com.wineadvisor.wineadvisor.DTO.users.fields.NameDTO;
import com.wineadvisor.wineadvisor.DTO.utils.PasswordDTO;
import com.wineadvisor.wineadvisor.DTO.utils.PictureDTO;
import com.wineadvisor.wineadvisor.model.users.User;
import com.wineadvisor.wineadvisor.model.users.fields.Dob;
import com.wineadvisor.wineadvisor.model.utils.Login;
import com.wineadvisor.wineadvisor.model.utils.Registered;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;

import lombok.Data;

@Data
@JsonPropertyOrder({ "name", "location", "email", "telephone", "username", "dob", "picture", "password" })
public class CreateUserDTO {
    @NotNull(message = "Name info cannot be null.")
    @Valid
    @Schema(name = "name", description = "Name info of the new user")
    @JsonProperty("name")
    private NameDTO nameDTO;

    @NotNull(message = "Address info cannot be null.")
    @Valid
    @Schema(name = "address", description = "Home address of the new user")
    @JsonProperty("address")
    private AddressDTO addressDTO;

    @NotBlank(message = "Email cannot be blank.")
    @Email(message = "Email must be a valid email address.")
    @Schema(description = "Email of the new user", example = "mariorossi@example.com")
    private String email;

    @Pattern(regexp = "^\\+?[0-9\\s\\-()]+$", message = "Telephone must be a valid telephone number.")
    @Schema(description = "Telephone of the new user", example = "+39 3331234567")
    private String telephone;

    @PastOrPresent(message = "Date must be in the past.")
    // @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z$", message = "Date must follow the format 'yyyy-MM-ddTHH:mm:ssZ'")
    @Schema(name = "date of birth", description = "Date of birth of the new user", example = "1970-01-01T00:00:00.000Z")
    @JsonProperty("date of birth")
    private LocalDateTime dob;

    @Schema(name = "profile picture", description = "Link of profile picture of the new user in 3 differents ratios")
    @JsonProperty("profile picture")
    private PictureDTO pictureDTO;

    @NotBlank(message = "Username cannot be blank.")
    @Pattern(regexp = "^[a-zA-Z0-9_]{3,50}$", message = "Username must be between 3 and 50 characters long and can contain letters, numbers, and underscores.")
    @Schema(description = "Username of the new user", example = "user123")
    private String username;

    @NotNull(message = "Password info cannot be null.")
    @Valid
    @Schema(name = "password", description = "Password info of the new user")
    @JsonProperty("password")
    private PasswordDTO passwordDTO;



    ///////////// METODI PUBBLICI /////////////
    // Ritorna un oggetto di tipo User a partire da un oggetto di tipo CreateUserDTO (inserisce i valori dei soli campi presenti anche nella classe CreateUserDTO).
    public User toUser() {
        User user = new User(
                null, // _id
                this.getNameDTO().toName(), // name
                this.getAddressDTO().toAddress(), // address
                this.email, // email
                this.telephone, // telephone
                new Login(this.username, null), // login
                new Registered(), // registered
                new Dob(), // dob
                this.getPictureDTO().toPicture(), // picture
                new ArrayList<>(), // reviews
                new ArrayList<>(), // likes
                new ArrayList<>() // dislikes
        );
        user.getDob().setDateTime(this.dob);
        
        return user;
    }
}
