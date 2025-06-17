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
        return email -> {
            AppUser user = appUserRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + email));

            return new org.springframework.security.core.userdetails.User(
                    user.getEmail(),
                    user.getPassword(),
                    Collections.emptyList()
            );
        };
    }
}
