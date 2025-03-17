package com.wineadvisor.wineadvisor.model;

import java.util.ArrayList;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "users")
public class User {
    @Id
    private Long _id;

    private Name name;

    private Location location;

    private String email;
    private String telephone;
    private Login login;
    private Registered registered;
    
    private Dob dob;

    private Picture picture;

    private ArrayList<Review> reviews;
}
