package com.wineadvisor.wineadvisor.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import com.wineadvisor.wineadvisor.exception.ResourceNotFoundException;
import com.wineadvisor.wineadvisor.model.users.User;
import com.wineadvisor.wineadvisor.model.wineries.Winery;
import com.wineadvisor.wineadvisor.repository.UserRepository;
import com.wineadvisor.wineadvisor.repository.WineryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationService implements UserDetailsService {
    ////////////////////////////////
    /////////// VARIABLES //////////
    ////////////////////////////////
    private final UserRepository userRepository;
    private final WineryRepository wineryRepository;



    ////////////////////////////////
    /////// PRIVATE METHODS ////////
    ////////////////////////////////
    
    ////////////////////////////////
    ///// Authentication logic /////
    
    // Costruisce un oggetto UserDetails a partire da un oggetto User
    private UserDetails buildUserDetails(User user) {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        // TODO: Uncomment the following if you want to add admin authentication
        // if (user.isAdmin()) {
        //     grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        // }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getLogin().getUsername())
                .password(user.getLogin().getPassword())
                .authorities(grantedAuthorities)
                .build();
    }

    // Costruisce un oggetto UserDetails a partire da un oggetto Winery
    private UserDetails buildUserDetails(Winery winery) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_WINERY"));

        return org.springframework.security.core.userdetails.User.builder()
                .username(winery.getLogin().getUsername())
                .password(winery.getLogin().getPassword())
                .authorities(authorities)
                .build();
    }

    // TODO: Uncomment the following if you want to add admin authentication
    // // Costruisce un oggetto UserDetails a partire da un oggetto Admin
    // private UserDetails buildUserDetails(Admin admin) {
    //     List<GrantedAuthority> authorities = new ArrayList<>();
    //     authorities.add(new SimpleGrantedAuthority(ROLE_ADMIN));

    //     return org.springframework.security.core.userdetails.User.builder()
    //             .username(admin.getLogin().getUsername())
    //             .password(admin.getLogin().getPassword())
    //             .authorities(authorities)
    //             .build();
    // }

    // END of authentication logic //
    /////////////////////////////////


    
    /////////////////////////////////
    //////// PUBLIC METHODS /////////
    /////////////////////////////////
    
    /////////////////////////////////
    /////// CRUD operations /////////
    
    /// READ operations ///
    // Recupera un utente dal database in base all'username fornito (override dell'omonimo metodo della classe UserDetailsService)
    @Override
    public UserDetails loadUserByUsername(String username) throws ResourceNotFoundException {
        User user = userRepository.findByLogin_Username(username).orElse(null);
        Winery winery = wineryRepository.findByLogin_Username(username).orElse(null);
        // TODO: Uncomment the following if you want to add admin authentication
        // Admin admin = adminRepository.findByLogin_Username(username).orElse(null);

        if (user != null) {
            return buildUserDetails(user);
        } else if (winery != null) {
            return buildUserDetails(winery);
        // TODO: Uncomment the following if you want to add admin authentication
        // } else if (admin != null) {
        //     return buildUserDetails(admin);
        } else {
            throw new ResourceNotFoundException("User with username \"" + username + "\" does not exist.");
        }
    }

    //// END of crud operations ////
    ////////////////////////////////
}
