package com.gamba.software.photoapp.services;

import com.gamba.software.photoapp.configs.JwtAuthenticationProvider;
import com.gamba.software.photoapp.controllers.dto.AuthenticationRequest;
import com.gamba.software.photoapp.controllers.dto.AuthenticationResponse;
import com.gamba.software.photoapp.controllers.dto.RegisterRequest;
import com.gamba.software.photoapp.exceptions.ResourceAlreadyExistsException;
import com.gamba.software.photoapp.repositories.AppUserRepository;
import com.gamba.software.photoapp.repositories.models.AppUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class AuthenticationService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtAuthenticationProvider jwtAuthenticationProvider;

    public AuthenticationService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder, JwtService jwtService, JwtAuthenticationProvider jwtAuthenticationProvider) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.jwtAuthenticationProvider = jwtAuthenticationProvider;
    }

    public AuthenticationResponse register(RegisterRequest request) {
        if (!appUserRepository.findByEmail(request.email()).isEmpty()) {
            throw new ResourceAlreadyExistsException("AppUser", "email", request.email());
        }
        AppUser appUser = new AppUser();
        appUser.setEmail(request.email());
        appUser.setUsername(request.username());
        appUser.setPassword(passwordEncoder.encode(request.password()));

        appUser = appUserRepository.save(appUser);
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                appUser.getEmail(),
                appUser.getPassword(),
                Collections.emptyList()
        );
        var jwtToken = jwtService.generateToken(userDetails);
        return new AuthenticationResponse(jwtToken);
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        jwtAuthenticationProvider.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        var appUser = appUserRepository.findByEmail(request.email())
                .orElseThrow();
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                appUser.getEmail(),
                appUser.getPassword(),
                Collections.emptyList()
        );
        var jwtToken = jwtService.generateToken(userDetails);
        return new AuthenticationResponse(jwtToken);
    }
}