package com.gamba.software.photoapp.configs;

import com.gamba.software.photoapp.exceptions.JwtAuthenticationException;
import com.gamba.software.photoapp.services.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;


import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationProviderTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private JwtAuthenticationProvider jwtAuthenticationProvider;

    @Mock
    private UserDetails userDetails;

    private Authentication authentication;
    private final String testJwt = "test.jwt.token";
    private final String testUsername = "testuser";

    @BeforeEach
    void setUp() {
        // Setup common mock behavior for UserDetails
        lenient().when(userDetails.getUsername()).thenReturn(testUsername);
        lenient().when(userDetails.getAuthorities()).thenReturn(List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void authenticate_successfulAuthentication_shouldReturnAuthenticatedToken() {
        authentication = new UsernamePasswordAuthenticationToken(null, testJwt);
        when(jwtService.extractUsername(testJwt)).thenReturn(testUsername);
        when(userDetailsService.loadUserByUsername(testUsername)).thenReturn(userDetails);
        when(jwtService.isTokenValid(testJwt, userDetails)).thenReturn(true);

        Authentication result = jwtAuthenticationProvider.authenticate(authentication);

        assertNotNull(result);
        assertTrue(result.isAuthenticated());
        assertEquals(userDetails, result.getPrincipal());
        assertEquals(testJwt, result.getCredentials());
        assertTrue(result.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void authenticate_whenJwtServiceIsTokenValidReturnsFalse_shouldThrowJwtAuthenticationException() {
        authentication = new UsernamePasswordAuthenticationToken(null, testJwt);
        when(jwtService.extractUsername(testJwt)).thenReturn(testUsername);
        when(userDetailsService.loadUserByUsername(testUsername)).thenReturn(userDetails);
        when(jwtService.isTokenValid(testJwt, userDetails)).thenReturn(false);

        JwtAuthenticationException exception = assertThrows(JwtAuthenticationException.class,
                () -> jwtAuthenticationProvider.authenticate(authentication));
        assertEquals("Invalid JWT token", exception.getMessage());
    }

    @Test
    void authenticate_withNullJwt_shouldThrowJwtAuthenticationException() {
        authentication = new UsernamePasswordAuthenticationToken(null, null);
        JwtAuthenticationException exception = assertThrows(JwtAuthenticationException.class,
                () -> jwtAuthenticationProvider.authenticate(authentication));
        assertEquals("Empty or null JWT", exception.getMessage());
    }

    @Test
    void authenticate_withEmptyJwt_shouldThrowJwtAuthenticationException() {
        authentication = new UsernamePasswordAuthenticationToken(null, "");
        JwtAuthenticationException exception = assertThrows(JwtAuthenticationException.class,
                () -> jwtAuthenticationProvider.authenticate(authentication));
        assertEquals("Empty or null JWT", exception.getMessage());
    }

    @Test
    void authenticate_withBlankJwt_shouldThrowJwtAuthenticationException() {
        authentication = new UsernamePasswordAuthenticationToken(null, "   ");
        JwtAuthenticationException exception = assertThrows(JwtAuthenticationException.class,
                () -> jwtAuthenticationProvider.authenticate(authentication));
        assertEquals("Empty or null JWT", exception.getMessage());
    }

    // This test is now covered by the MalformedJwtException test from jwtService.extractUsername
    // The provider no longer has a separate "Missing parts" check.
    // @Test
    // void authenticate_withMalformedJwtMissingParts_shouldThrowJwtAuthenticationException() {
    //     authentication = new UsernamePasswordAuthenticationToken(null, "invalidjwt");
    //     JwtAuthenticationException exception = assertThrows(JwtAuthenticationException.class,
    //             () -> jwtAuthenticationProvider.authenticate(authentication));
    //     assertEquals("Malformed JWT: Missing parts", exception.getMessage());
    // }

    @Test
    void authenticate_whenJwtServiceExtractUsernameThrowsMalformedJwtException_shouldThrowJwtAuthenticationException() {
        authentication = new UsernamePasswordAuthenticationToken(null, testJwt);
        when(jwtService.extractUsername(testJwt)).thenThrow(new MalformedJwtException("Test Malformed"));

        JwtAuthenticationException exception = assertThrows(JwtAuthenticationException.class,
                () -> jwtAuthenticationProvider.authenticate(authentication));
        assertEquals("Malformed JWT: Test Malformed", exception.getMessage());
        assertTrue(exception.getCause() instanceof MalformedJwtException);
    }

    @Test
    void authenticate_whenJwtServiceExtractUsernameThrowsExpiredJwtException_shouldThrowJwtAuthenticationException() {
        authentication = new UsernamePasswordAuthenticationToken(null, testJwt);
        // Mocking claims for the ExpiredJwtException constructor
        io.jsonwebtoken.Claims claims = mock(io.jsonwebtoken.Claims.class);
        io.jsonwebtoken.Header header = mock(io.jsonwebtoken.Header.class);
        when(jwtService.extractUsername(testJwt)).thenThrow(new ExpiredJwtException(header, claims, "Test Expired"));

        JwtAuthenticationException exception = assertThrows(JwtAuthenticationException.class,
                () -> jwtAuthenticationProvider.authenticate(authentication));
        assertEquals("Expired JWT: Test Expired", exception.getMessage());
        assertTrue(exception.getCause() instanceof ExpiredJwtException);
    }

    @Test
    void authenticate_whenJwtServiceExtractUsernameThrowsSignatureException_shouldThrowJwtAuthenticationException() {
        authentication = new UsernamePasswordAuthenticationToken(null, testJwt);
        when(jwtService.extractUsername(testJwt)).thenThrow(new SignatureException("Test Signature Invalid"));

        JwtAuthenticationException exception = assertThrows(JwtAuthenticationException.class,
                () -> jwtAuthenticationProvider.authenticate(authentication));
        assertEquals("JWT signature validation failed: Test Signature Invalid", exception.getMessage());
        assertTrue(exception.getCause() instanceof SignatureException);
    }


    @Test
    void authenticate_whenUserDetailsServiceThrowsUsernameNotFoundException_shouldThrowUsernameNotFoundException() {
        authentication = new UsernamePasswordAuthenticationToken(null, testJwt);
        when(jwtService.extractUsername(testJwt)).thenReturn(testUsername);
        when(userDetailsService.loadUserByUsername(testUsername)).thenThrow(new UsernameNotFoundException("User not found"));

        assertThrows(UsernameNotFoundException.class, // It should re-throw UsernameNotFoundException as per Spring Security conventions
                () -> jwtAuthenticationProvider.authenticate(authentication));
    }

    @Test
    void supports_withUsernamePasswordAuthenticationToken_shouldReturnTrue() {
        assertTrue(jwtAuthenticationProvider.supports(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void supports_withOtherAuthenticationType_shouldReturnFalse() {
        // Example with a different Authentication type
        class OtherAuthenticationToken implements Authentication {
            @Override public List<GrantedAuthority> getAuthorities() { return Collections.emptyList(); }
            @Override public Object getCredentials() { return null; }
            @Override public Object getDetails() { return null; }
            @Override public Object getPrincipal() { return null; }
            @Override public boolean isAuthenticated() { return false; }
            @Override public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {}
            @Override public String getName() { return null; }
        }
        assertFalse(jwtAuthenticationProvider.supports(OtherAuthenticationToken.class));
    }
}
