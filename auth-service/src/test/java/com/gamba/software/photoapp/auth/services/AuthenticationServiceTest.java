package com.gamba.software.photoapp.auth.services;

import com.gamba.software.photoapp.auth.configs.JwtAuthenticationProvider;
import com.gamba.software.photoapp.auth.controllers.dto.UserValidationResponse;
import com.gamba.software.photoapp.auth.exceptions.JwtAuthenticationException;
import com.gamba.software.photoapp.auth.repositories.AppUserRepository;
import com.gamba.software.photoapp.auth.security.AuthJwtService; // Updated import
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private AppUserRepository appUserRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthJwtService jwtService; // Updated type
    @Mock
    private JwtAuthenticationProvider jwtAuthenticationProvider; // Mocked, but not directly used by validateTokenAndExtractUserDetails
    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private AuthenticationService authenticationService;

    private UserDetails userDetails;
    private final String testToken = "test.jwt.token";
    private final String testEmail = "test@example.com";

    @BeforeEach
    void setUp() {
        userDetails = new User(testEmail, "password", List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void validateTokenAndExtractUserDetails_success() {
        when(jwtService.extractUsername(testToken)).thenReturn(testEmail);
        when(userDetailsService.loadUserByUsername(testEmail)).thenReturn(userDetails);
        when(jwtService.isTokenValid(testToken, userDetails)).thenReturn(true);

        UserValidationResponse response = authenticationService.validateTokenAndExtractUserDetails(testToken);

        assertNotNull(response);
        assertEquals(testEmail, response.username());
        assertTrue(response.authorities().contains("ROLE_USER"));
        verify(jwtService).extractUsername(testToken);
        verify(userDetailsService).loadUserByUsername(testEmail);
        verify(jwtService).isTokenValid(testToken, userDetails);
    }

    @Test
    void validateTokenAndExtractUserDetails_usernameNullFromToken() {
        when(jwtService.extractUsername(testToken)).thenReturn(null);

        JwtAuthenticationException exception = assertThrows(JwtAuthenticationException.class,
                () -> authenticationService.validateTokenAndExtractUserDetails(testToken));
        assertTrue(exception.getMessage().contains("Username could not be extracted"));
    }

    @Test
    void validateTokenAndExtractUserDetails_userNotFound() {
        when(jwtService.extractUsername(testToken)).thenReturn(testEmail);
        when(userDetailsService.loadUserByUsername(testEmail)).thenThrow(new UsernameNotFoundException("User not found"));

        JwtAuthenticationException exception = assertThrows(JwtAuthenticationException.class,
                () -> authenticationService.validateTokenAndExtractUserDetails(testToken));
        assertTrue(exception.getMessage().contains("User not found for token"));
        assertTrue(exception.getCause() instanceof UsernameNotFoundException);
    }

    @Test
    void validateTokenAndExtractUserDetails_tokenNotValidForUser() {
        when(jwtService.extractUsername(testToken)).thenReturn(testEmail);
        when(userDetailsService.loadUserByUsername(testEmail)).thenReturn(userDetails);
        when(jwtService.isTokenValid(testToken, userDetails)).thenReturn(false); // Token is not valid for this user

        JwtAuthenticationException exception = assertThrows(JwtAuthenticationException.class,
                () -> authenticationService.validateTokenAndExtractUserDetails(testToken));
        assertTrue(exception.getMessage().contains("Token is not valid for the user"));
    }

    @Test
    void validateTokenAndExtractUserDetails_expiredJwt() {
        when(jwtService.extractUsername(testToken)).thenThrow(ExpiredJwtException.class);

        JwtAuthenticationException exception = assertThrows(JwtAuthenticationException.class,
                () -> authenticationService.validateTokenAndExtractUserDetails(testToken));
        assertTrue(exception.getMessage().contains("Expired JWT"));
        assertTrue(exception.getCause() instanceof ExpiredJwtException);
    }

    @Test
    void validateTokenAndExtractUserDetails_malformedJwt() {
        when(jwtService.extractUsername(testToken)).thenThrow(MalformedJwtException.class);

        JwtAuthenticationException exception = assertThrows(JwtAuthenticationException.class,
                () -> authenticationService.validateTokenAndExtractUserDetails(testToken));
        assertTrue(exception.getMessage().contains("Malformed JWT"));
        assertTrue(exception.getCause() instanceof MalformedJwtException);
    }

    @Test
    void validateTokenAndExtractUserDetails_signatureException() {
        // This case might also be caught by isTokenValid if extractUsername doesn't throw first
        // For this test, assume extractUsername throws it.
        when(jwtService.extractUsername(testToken)).thenThrow(SignatureException.class);

        JwtAuthenticationException exception = assertThrows(JwtAuthenticationException.class,
                () -> authenticationService.validateTokenAndExtractUserDetails(testToken));
        assertTrue(exception.getMessage().contains("JWT signature validation failed"));
        assertTrue(exception.getCause() instanceof SignatureException);
    }

    // Test for register and authenticate methods can also be added here for completeness,
    // but the primary focus for this task is the new validation method.
}
