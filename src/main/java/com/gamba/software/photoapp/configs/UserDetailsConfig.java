package com.gamba.software.photoapp.configs;

import com.gamba.software.photoapp.repositories.AppUserRepository;
import com.gamba.software.photoapp.repositories.models.AppUser;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class UserDetailsConfig {
    private final AppUserRepository appUserRepository;

    public UserDetailsConfig(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    // Bean for User Details Service
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            AppUser user = appUserRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

            return new org.springframework.security.core.userdetails.User(
                    user.getUsername(),
                    user.getPassword(), // Password here is the HASHED password from the DB
                    Collections.emptyList()
            );
        };
    }
}
