package com.wineadvisor.wineadvisor.model;

import java.util.ArrayList;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.wineadvisor.wineadvisor.model.fields.users.Login;
import com.wineadvisor.wineadvisor.model.fields.users.Picture;
import com.wineadvisor.wineadvisor.model.fields.users.Registered;

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

    private String name;

    private String address;

    private String city;

    private String zipcode;

    private String province;

    private String region;

    private String country;

    private String telephone;

    private String email;

    private String website;

    private String facebook;

    private String instagram;

    private ArrayList<String> images;

    private Login login;

    private Registered registered;

    private Picture picture;
}
