package com.wineadvisor.wineadvisor.DTO.users;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.wineadvisor.wineadvisor.DTO.users.fields.LocationDTO;
import com.wineadvisor.wineadvisor.DTO.users.fields.NameDTO;
import com.wineadvisor.wineadvisor.DTO.utils.PictureDTO;
import com.wineadvisor.wineadvisor.model.users.User;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@JsonPropertyOrder({ "name", "location", "email", "telephone", "dob", "picture" })
public class UpdateUserDTO {
    @NotNull(message = "Name info cannot be blank.")
    @Valid
    @Schema(name = "name", description = "Name info of the updated user")
    @JsonProperty("name")
    private NameDTO nameDTO;

    @NotNull(message = "Location info cannot be blank.")
    @Valid
    @Schema(name = "location", description = "Home address of the updated user")
    @JsonProperty("location")
    private LocationDTO locationDTO;

    @NotBlank(message = "Email cannot be blank.")
    @Email(message = "Email must be a valid email address.")
    @Schema(description = "Email of the updated user", example = "mariorossi@example.com")
    private String email;

    @Pattern(regexp = "^\\+?[0-9\\s\\-()]+$", message = "Telephone must be a valid telephone number.")
    @Schema(description = "Telephone of the updated user", example = "+39 3331234567")
    private String telephone;

    @PastOrPresent(message = "Date must be in the past.")
    // @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z$", message = "Date must follow the format 'yyyy-MM-ddTHH:mm:ssZ'")
    @Schema(name = "date of birth", description = "Date of birth of the updated user", example = "1970-01-01T00:00:00.000Z")
    @JsonProperty("dob")
    private LocalDateTime dob;

    @Schema(name = "picture", description = "Picture info of the updated user")
    @JsonProperty("picture")
    private PictureDTO pictureDTO;



    ///////////// METODI PUBBLICI /////////////
    // Modifica l'oggetto di classe User passato come argomento sostituendo i campi aggiornabili con i valori aggiornati (quelli dell'istanza attuale (this.)). Ritorna l'utente aggiornato.
    public User toUser(User targetUser) {
        targetUser.setName(this.getNameDTO().toName());
        targetUser.setLocation(this.getLocationDTO().toLocation());
        targetUser.setEmail(this.getEmail());
        targetUser.setTelephone(this.getTelephone());
        targetUser.getDob().setDateTime(this.getDob());
        targetUser.setPicture(this.getPictureDTO().toPicture());

        return targetUser;
    }
}
