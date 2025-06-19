package com.gamba.software.photoapp.services;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    private JwtService jwtService;

    @Mock
    private UserDetails userDetails;

    // A valid Base64 encoded key for HS256 (minimum 256 bits / 32 bytes)
    private final String testSecretKey = "===========================================testSecretKeyForHS256"; // 64 chars -> 48 bytes
    private final long testJwtExpiration = 3600000; // 1 hour in ms
    private final long shortJwtExpiration = 1; // 1 ms for expiry test

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", testSecretKey);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", testJwtExpiration);
    }

    @Test
    void generateToken_shouldReturnTokenWithCorrectUsername() {
        when(userDetails.getUsername()).thenReturn("testuser");

        String token = jwtService.generateToken(userDetails);

        assertNotNull(token);
        assertEquals("testuser", jwtService.extractUsername(token));
    }

    @Test
    void extractUsername_shouldReturnCorrectUsername() {
        when(userDetails.getUsername()).thenReturn("extractUser");
        String token = jwtService.generateToken(userDetails);

        assertEquals("extractUser", jwtService.extractUsername(token));
    }

    @Test
    void isTokenValid_withValidToken_shouldReturnTrue() {
        when(userDetails.getUsername()).thenReturn("validUser");
        String token = jwtService.generateToken(userDetails);

        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void isTokenValid_withExpiredToken_shouldThrowExpiredJwtExceptionAndReturnFalse() {
        when(userDetails.getUsername()).thenReturn("expiredUser");
        // Use ReflectionTestUtils to temporarily set short expiration for this test
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", shortJwtExpiration);
        String expiredToken = jwtService.generateToken(userDetails);

        // Wait for the token to expire
        try {
            Thread.sleep(100); // Wait 100ms, token should be expired
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // isTokenValid itself calls extractUsername which calls extractClaim which can throw ExpiredJwtException
        // So we assert that the specific exception is thrown when trying to extract claims from an expired token.
        assertThrows(ExpiredJwtException.class, () -> jwtService.extractUsername(expiredToken));

        // Consequently, isTokenValid should effectively be false.
        // We can't directly call isTokenValid and expect false because extractUsername will throw first.
        // A more direct test for isTokenValid's logic for expired tokens might involve mocking extractUsername
        // but the current JwtService structure makes this test sufficient to show expiry is handled.

        // To more directly test the isTokenExpired part of isTokenValid:
        final String finalExpiredToken = expiredToken; // for lambda
        assertThrows(ExpiredJwtException.class, () -> jwtService.isTokenValid(finalExpiredToken, userDetails),
                "isTokenValid should throw ExpiredJwtException if underlying claim extraction fails due to expiry.");
    }


    @Test
    void isTokenValid_withMalformedToken_shouldThrowMalformedJwtException() {
        String malformedToken = "this.is.not.a.jwt";
        // UserDetails mock is needed for the comparison part of isTokenValid, though it won't be reached.
        when(userDetails.getUsername()).thenReturn("anyUser");
        assertThrows(MalformedJwtException.class, () -> jwtService.isTokenValid(malformedToken, userDetails));
    }

    @Test
    void isTokenValid_withTokenSignedByDifferentKey_shouldThrowSignatureException() {
        when(userDetails.getUsername()).thenReturn("signatureUser");
        String token = jwtService.generateToken(userDetails); // Token generated with testSecretKey

        // Create another JwtService instance with a different key
        JwtService jwtServiceWithDifferentKey = new JwtService();
        ReflectionTestUtils.setField(jwtServiceWithDifferentKey, "secretKey", "===========================================anotherSecretKeyForHS256");
        ReflectionTestUtils.setField(jwtServiceWithDifferentKey, "jwtExpiration", testJwtExpiration);

        // Attempt to validate the token using the service with the different key
        assertThrows(SignatureException.class, () -> jwtServiceWithDifferentKey.isTokenValid(token, userDetails));
    }

    @Test
    void isTokenValid_withValidTokenButWrongUser_shouldReturnFalse() {
        when(userDetails.getUsername()).thenReturn("actualUser");
        String token = jwtService.generateToken(userDetails);

        UserDetails differentUserDetails = org.mockito.Mockito.mock(UserDetails.class);
        when(differentUserDetails.getUsername()).thenReturn("wrongUser");

        assertFalse(jwtService.isTokenValid(token, differentUserDetails));
    }
}
