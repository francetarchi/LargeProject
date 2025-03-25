package com.wineadvisor.wineadvisor.model;

import java.util.ArrayList;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Document(collection = "users")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User {
    @Id
    private ObjectId _id;

    private Name name;

    private Location location;

    @Schema(description = "email", example = "mariorossi@example.com")
    private String email;

    @Schema(description = "telephone", example = "+39 3331234567")
    private String telephone;
    
    private Login login;
    private Registered registered;
    
    private Dob dob;

    private Picture picture;

    // private ArrayList<Review> reviews;
}
