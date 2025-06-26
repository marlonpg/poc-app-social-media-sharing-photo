package com.gamba.software.photoapp.photos.configs;

import com.gamba.software.photoapp.photos.model.UserValidationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PhotoUserDetailsServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private PhotoUserDetailsService photoUserDetailsService;

    private final String authServiceBaseUrl = "http://fake-auth-service/api/v1/auth";
    private final String token = "valid.jwt.token";

    @BeforeEach
    void setUp() {
        photoUserDetailsService = new PhotoUserDetailsService(restTemplate, authServiceBaseUrl);
    }

    @Test
    void loadUserByUsername_success() {
        UserValidationResponse mockResponseDto = new UserValidationResponse("testuser", List.of("ROLE_USER", "ROLE_EDITOR"));
        ResponseEntity<UserValidationResponse> mockResponseEntity = new ResponseEntity<>(mockResponseDto, HttpStatus.OK);

        HttpHeaders expectedHeaders = new HttpHeaders();
        expectedHeaders.set("Authorization", "Bearer " + token);
        HttpEntity<String> expectedEntity = new HttpEntity<>(expectedHeaders);

        when(restTemplate.exchange(
                eq(authServiceBaseUrl + "/validate"),
                eq(HttpMethod.POST),
                eq(expectedEntity),
                eq(UserValidationResponse.class)
        )).thenReturn(mockResponseEntity);

        UserDetails userDetails = photoUserDetailsService.loadUserByUsername(token);

        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_USER")));
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_EDITOR")));
        assertEquals(2, userDetails.getAuthorities().size());
    }

    @Test
    void loadUserByUsername_emptyToken_throwsUsernameNotFoundException() {
        Exception exception = assertThrows(UsernameNotFoundException.class, () -> {
            photoUserDetailsService.loadUserByUsername("");
        });
        assertEquals("Token cannot be empty", exception.getMessage());

        Exception nullTokenException = assertThrows(UsernameNotFoundException.class, () -> {
            photoUserDetailsService.loadUserByUsername(null);
        });
        assertEquals("Token cannot be empty", nullTokenException.getMessage());
    }

    @Test
    void loadUserByUsername_authServiceReturnsUnauthorized_throwsUsernameNotFoundException() {
        HttpHeaders expectedHeaders = new HttpHeaders();
        expectedHeaders.set("Authorization", "Bearer " + token);
        HttpEntity<String> expectedEntity = new HttpEntity<>(expectedHeaders);

        when(restTemplate.exchange(
                eq(authServiceBaseUrl + "/validate"),
                eq(HttpMethod.POST),
                eq(expectedEntity),
                eq(UserValidationResponse.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Token expired"));

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            photoUserDetailsService.loadUserByUsername(token);
        });
        assertTrue(exception.getMessage().contains("Failed to validate token with auth-service: 401 UNAUTHORIZED"));
    }

    @Test
    void loadUserByUsername_authServiceReturnsNullBody_throwsUsernameNotFoundException() {
        ResponseEntity<UserValidationResponse> mockResponseEntity = new ResponseEntity<>(null, HttpStatus.OK);
        HttpHeaders expectedHeaders = new HttpHeaders();
        expectedHeaders.set("Authorization", "Bearer " + token);
        HttpEntity<String> expectedEntity = new HttpEntity<>(expectedHeaders);

        when(restTemplate.exchange(
                eq(authServiceBaseUrl + "/validate"),
                eq(HttpMethod.POST),
                eq(expectedEntity),
                eq(UserValidationResponse.class)
        )).thenReturn(mockResponseEntity);

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            photoUserDetailsService.loadUserByUsername(token);
        });
        assertEquals("User details not found from token via auth-service.", exception.getMessage());
    }

    @Test
    void loadUserByUsername_authServiceReturnsResponseWithNullUsername_throwsUsernameNotFoundException() {
        UserValidationResponse mockResponseDto = new UserValidationResponse(null, Collections.singletonList("ROLE_USER"));
        ResponseEntity<UserValidationResponse> mockResponseEntity = new ResponseEntity<>(mockResponseDto, HttpStatus.OK);
        HttpHeaders expectedHeaders = new HttpHeaders();
        expectedHeaders.set("Authorization", "Bearer " + token);
        HttpEntity<String> expectedEntity = new HttpEntity<>(expectedHeaders);

        when(restTemplate.exchange(
                eq(authServiceBaseUrl + "/validate"),
                eq(HttpMethod.POST),
                eq(expectedEntity),
                eq(UserValidationResponse.class)
        )).thenReturn(mockResponseEntity);

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            photoUserDetailsService.loadUserByUsername(token);
        });
        assertEquals("User details not found from token via auth-service.", exception.getMessage());
    }

    @Test
    void loadUserByUsername_restTemplateThrowsGenericException_throwsUsernameNotFoundException() {
        HttpHeaders expectedHeaders = new HttpHeaders();
        expectedHeaders.set("Authorization", "Bearer " + token);
        HttpEntity<String> expectedEntity = new HttpEntity<>(expectedHeaders);

        when(restTemplate.exchange(
                eq(authServiceBaseUrl + "/validate"),
                eq(HttpMethod.POST),
                eq(expectedEntity),
                eq(UserValidationResponse.class)
        )).thenThrow(new RuntimeException("Network error"));

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            photoUserDetailsService.loadUserByUsername(token);
        });
        assertEquals("Unexpected error validating token.", exception.getMessage());
        assertTrue(exception.getCause() instanceof RuntimeException);
    }
}
