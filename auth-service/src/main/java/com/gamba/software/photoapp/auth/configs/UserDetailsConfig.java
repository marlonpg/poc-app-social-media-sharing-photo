package com.gamba.software.photoapp.auth.configs;

import com.gamba.software.photoapp.auth.repositories.AppUserRepository;
import com.gamba.software.photoapp.auth.repositories.models.AppUser;
import com.gamba.software.photoapp.auth.repositories.models.Role;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

            List<GrantedAuthority> authorities = user.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority(role.getName()))
                    .collect(Collectors.toList());

            return new org.springframework.security.core.userdetails.User(
                    user.getEmail(),
                    user.getPassword(),
                    authorities
            );
        };
    }
}
