package com.wineadvisor.wineadvisor.model;

import java.util.ArrayList;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.wineadvisor.wineadvisor.model.fields.users.Login;
import com.wineadvisor.wineadvisor.model.fields.users.Picture;
import com.wineadvisor.wineadvisor.model.fields.users.Registered;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Document(collection = "wineries")
@AllArgsConstructor
@NoArgsConstructor
public class Winery {
    @Id
    @Field("_id")
    private Long id;

    @NotNull(message = "Name info cannot be blank.")
    @Valid
    private String name;

    @NotNull(message = "Address info cannot be blank.")
    @Valid
    private String address;

    private String city;

    private String zipcode;

    private String province;

    private String region;

    private String country;

    @Pattern(regexp = "^\\+?[0-9\\s\\-()]+$", message = "Telephone must be a valid telephone number.")
    @Schema(description = "telephone", example = "+39 3331234567")
    private String telephone;

    @NotBlank(message = "Email cannot be blank.")
    @Email(message = "Email must be a valid email address.")
    @Schema(description = "email", example = "mariorossi@example.com")
    private String email;

    private String website;

    private String facebook;

    private String instagram;

    private ArrayList<String> images;

    @NotNull(message = "Login info cannot be blank.")
    @Valid
    private Login login;

    @Valid
    private Registered registered;

    private Picture picture;
}
