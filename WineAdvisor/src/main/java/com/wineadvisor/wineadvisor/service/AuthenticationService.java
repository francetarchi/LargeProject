package com.wineadvisor.wineadvisor.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import com.wineadvisor.wineadvisor.exception.ResourceNotFoundException;
import com.wineadvisor.wineadvisor.model.admin.Admin;
import com.wineadvisor.wineadvisor.model.users.User;
import com.wineadvisor.wineadvisor.model.wineries.Winery;
import com.wineadvisor.wineadvisor.repository.AdminRepository;
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
    private final AdminRepository adminRepository;



    ////////////////////////////////
    /////// PRIVATE METHODS ////////
    ////////////////////////////////
    
    ////////////////////////////////
    ///// Authentication logic /////
    
    // Costruisce un oggetto UserDetails a partire da un oggetto User
    private UserDetails buildUserDetails(User user) {
        System.out.println("--- INFO: Processing USER login for account \"" + user.getLogin().getUsername() + "\".");

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getLogin().getUsername())
                .password(user.getLogin().getPassword())
                .roles("USER")
                .build();
    }

    // Costruisce un oggetto UserDetails a partire da un oggetto Winery
    private UserDetails buildUserDetails(Winery winery) {
        System.out.println("--- INFO: Processing WINERY login for account \"" + winery.getLogin().getUsername() + "\".");

        return org.springframework.security.core.userdetails.User.builder()
                .username(winery.getLogin().getUsername())
                .password(winery.getLogin().getPassword())
                .roles("WINERY")
                .build();
    }
    
    // Costruisce un oggetto UserDetails a partire da un oggetto Admin
    private UserDetails buildUserDetails(Admin admin) {
        System.out.println("--- INFO: Processing ADMIN login for account \"" + admin.getLogin().getUsername() + "\".");

        return org.springframework.security.core.userdetails.User.builder()
                .username(admin.getLogin().getUsername())
                .password(admin.getLogin().getPassword())
                .roles("ADMIN")
                .build();
    }

    // END of authentication logic //
    /////////////////////////////////


    
    /////////////////////////////////
    //////// PUBLIC METHODS /////////
    /////////////////////////////////
    
    /////////////////////////////////
    /////// Util operations /////////
    
    // Recupera un account (che sia User, Winery o Admin) dal database in base all'username fornito (override dell'omonimo metodo della classe UserDetailsService)
    @Override
    public UserDetails loadUserByUsername(String username) throws ResourceNotFoundException {
        System.out.println("--- INFO: Searching for account with username: \"" + username + "\".");

        User user = userRepository.findByLogin_Username(username).orElse(null);
        Winery winery = wineryRepository.findByLogin_Username(username).orElse(null);
        Admin admin = adminRepository.findByLogin_Username(username).orElse(null);

        UserDetails userDetails = null;
        Boolean accountFound = false;
        if (user != null) {

            userDetails = buildUserDetails(user);
            accountFound = true;
        } else if (winery != null) {

            userDetails = buildUserDetails(winery);
            accountFound = true;
        } else if (admin != null) {

            userDetails = buildUserDetails(admin);
            accountFound = true;
        }

        if (accountFound) {
            System.out.println("--- INFO: Account found successfully in a collection.\n\n");
            return userDetails;
        } else {
            System.out.println("--- WRN: Authentication failed.");
            throw new ResourceNotFoundException("Account with username \"" + username + "\" does not exist in any collection.");
        }
    }

    //// END of util operations ////
    ////////////////////////////////
}
