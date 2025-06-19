package com.gamba.software.photoapp.auth.services;

import com.gamba.software.photoapp.auth.configs.JwtAuthenticationProvider;
import com.gamba.software.photoapp.auth.controllers.dto.AuthenticationRequest;
import com.gamba.software.photoapp.auth.controllers.dto.AuthenticationResponse;
import com.gamba.software.photoapp.auth.controllers.dto.RegisterRequest;
import com.gamba.software.photoapp.auth.exceptions.ResourceAlreadyExistsException;
import com.gamba.software.photoapp.auth.repositories.AppUserRepository;
import com.gamba.software.photoapp.auth.repositories.models.AppUser;
import com.gamba.software.photoapp.shared.jwt.JwtService; // Added import
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService; // Use import, no FQN needed here now
    private final JwtAuthenticationProvider jwtAuthenticationProvider;
    private final UserDetailsService userDetailsService;

    public AuthenticationService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder, JwtService jwtService, JwtAuthenticationProvider jwtAuthenticationProvider, UserDetailsService userDetailsService) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.jwtAuthenticationProvider = jwtAuthenticationProvider;
        this.userDetailsService = userDetailsService;
    }

    public AuthenticationResponse register(RegisterRequest request) {
        if (!appUserRepository.findByEmail(request.email()).isEmpty()) {
            throw new ResourceAlreadyExistsException("AppUser", "email", request.email());
        }
        AppUser appUser = new AppUser();
        appUser.setEmail(request.email());
        appUser.setUsername(request.username());
        appUser.setPassword(passwordEncoder.encode(request.password()));
        appUserRepository.save(appUser);

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());
        String jwtToken = jwtService.generateToken(userDetails);

        return new AuthenticationResponse(jwtToken);
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        jwtAuthenticationProvider.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());
        String jwtToken = jwtService.generateToken(userDetails);

        return new AuthenticationResponse(jwtToken);
    }
}
