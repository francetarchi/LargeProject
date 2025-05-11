package com.wineadvisor.wineadvisor.DTO.admins;

import java.time.Instant;

import com.wineadvisor.wineadvisor.DTO.utils.NameDTO;
import com.wineadvisor.wineadvisor.DTO.utils.PictureDTO;
import com.wineadvisor.wineadvisor.model.admin.Admin;

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
@JsonPropertyOrder({ "gender", "name", "email", "telephone", "date of birth", "profile picture" })
public class UpdateAdminDTO {
    @Pattern(regexp = "^(male|female|other)$", message = "Gender must be one among \"male\", \"female\" and \"other\" (or blank).")
    @Schema(description = "Gender of the updated admin", example = "male")
    private String gender;

    @NotNull(message = "Name info cannot be null.")
    @Valid
    @Schema(name = "name", description = "Name info of the updated admin")
    @JsonProperty("name")
    private NameDTO nameDTO;

    @NotBlank(message = "Email cannot be blank.")
    @Email(message = "Email must be a valid email address.")
    @Schema(description = "Email of the updated admin", example = "mariorossi@example.com")
    private String email;

    @Pattern(regexp = "^\\+?[0-9\\s\\-()]+$", message = "Telephone must be a valid telephone number.")
    @Schema(description = "Telephone of the updated admin", example = "+39 3331234567")
    private String telephone;

    @PastOrPresent(message = "Date must be in the past.")
    @Schema(name = "date of birth", description = "Date of birth of the updated admin", example = "1970-01-01T00:00:00.000Z")
    @JsonProperty("date of birth")
    private Instant dob;

    @Schema(name = "profile picture", description = "Picture info of the updated admin")
    @JsonProperty("profile picture")
    private PictureDTO pictureDTO;



    ///////////// METODI PUBBLICI /////////////
    // Modifica l'oggetto di classe Admin passato come argomento sostituendo i campi aggiornabili con i valori aggiornati (quelli dell'istanza attuale (this.)).
    // Ritorna l'admin aggiornato.
    public Admin toAdmin(Admin targetAdmin) {
        targetAdmin.setGender(this.getGender());
        targetAdmin.setName(this.getNameDTO().toName());
        targetAdmin.setEmail(this.getEmail());
        targetAdmin.setTelephone(this.getTelephone());
        targetAdmin.getDob().setDateTime(this.getDob());
        targetAdmin.setPicture(this.getPictureDTO().toPicture());

        return targetAdmin;
    }
}
