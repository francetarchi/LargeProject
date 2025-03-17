package com.wineadvisor.wineadvisor.model;

import jakarta.persistence.Id;
import lombok.Data;

@Data
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

    private Review reviews;
}
