package com.gamba.software.photoapp.auth.services;

import com.gamba.software.photoapp.auth.configs.JwtAuthenticationProvider;
import com.gamba.software.photoapp.auth.controllers.dto.AuthenticationRequest;
import com.gamba.software.photoapp.auth.controllers.dto.AuthenticationResponse;
import com.gamba.software.photoapp.auth.controllers.dto.RegisterRequest;
import com.gamba.software.photoapp.auth.controllers.dto.UserValidationResponse;
import com.gamba.software.photoapp.auth.exceptions.ResourceAlreadyExistsException;
import com.gamba.software.photoapp.auth.repositories.AppUserRepository;
import com.gamba.software.photoapp.auth.repositories.models.AppUser;
import com.gamba.software.photoapp.auth.security.AuthJwtService; // Updated import
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthJwtService jwtService; // Changed type to AuthJwtService
    private final JwtAuthenticationProvider jwtAuthenticationProvider;
    private final UserDetailsService userDetailsService;

    public AuthenticationService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder, AuthJwtService jwtService, JwtAuthenticationProvider jwtAuthenticationProvider, UserDetailsService userDetailsService) { // Changed type in constructor
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService; // Changed type here
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

    public UserValidationResponse validateTokenAndExtractUserDetails(String token) throws AuthenticationException {
        try {
            final String username = jwtService.extractUsername(token);
            if (username == null) {
                throw new JwtException("Username could not be extracted from token.");
            }

            UserDetails userDetails = userDetailsService.loadUserByUsername(username); // Can throw UsernameNotFoundException

            if (jwtService.isTokenValid(token, userDetails)) {
                return new UserValidationResponse(
                        userDetails.getUsername(),
                        userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList()
                );
            } else {
                // This case implies token is structurally valid and not expired, but failed some other check in isTokenValid
                throw new JwtException("Token is not valid for the user.");
            }
        } catch (ExpiredJwtException e) {
            throw new com.gamba.software.photoapp.auth.exceptions.JwtAuthenticationException("Expired JWT: " + e.getMessage(), e);
        } catch (SignatureException e) {
            throw new com.gamba.software.photoapp.auth.exceptions.JwtAuthenticationException("JWT signature validation failed: " + e.getMessage(), e);
        } catch (MalformedJwtException e) {
            throw new com.gamba.software.photoapp.auth.exceptions.JwtAuthenticationException("Malformed JWT: " + e.getMessage(), e);
        } catch (UsernameNotFoundException e) {
            // This is an AuthenticationException, so it's fine to rethrow or wrap
            throw new com.gamba.software.photoapp.auth.exceptions.JwtAuthenticationException("User not found for token: " + e.getMessage(), e);
        } catch (JwtException e) { // Catch-all for other JWT errors from jwtService
            throw new com.gamba.software.photoapp.auth.exceptions.JwtAuthenticationException("Invalid JWT: " + e.getMessage(), e);
        }
        // Explicitly not catching generic Exception here to let controller handle it as 500 if something unexpected occurs.
    }
}
