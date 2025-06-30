package com.gamba.software.photoapp.auth.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamba.software.photoapp.auth.controllers.dto.AuthenticationRequest;
import com.gamba.software.photoapp.auth.controllers.dto.AuthenticationResponse;
import com.gamba.software.photoapp.auth.controllers.dto.RegisterRequest;
import com.gamba.software.photoapp.auth.controllers.dto.UserValidationResponse;
import com.gamba.software.photoapp.auth.exceptions.JwtAuthenticationException;
import com.gamba.software.photoapp.auth.services.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthenticationController.class)
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationService authenticationService;

    @Autowired
    private ObjectMapper objectMapper;

    private RegisterRequest registerRequest;
    private AuthenticationRequest authenticationRequest;
    private AuthenticationResponse authenticationResponse;
    private UserValidationResponse userValidationResponse;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest("testuser", "test@example.com", "password123");
        authenticationRequest = new AuthenticationRequest("test@example.com", "password123");
        authenticationResponse = new AuthenticationResponse("dummy.jwt.token");
        userValidationResponse = new UserValidationResponse("test@example.com", List.of("ROLE_USER"));
    }

    @Test
    void register_shouldReturnToken_whenSuccessful() throws Exception {
        when(authenticationService.register(any(RegisterRequest.class))).thenReturn(authenticationResponse);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("dummy.jwt.token"));
    }

    @Test
    void authenticate_shouldReturnToken_whenSuccessful() throws Exception {
        when(authenticationService.authenticate(any(AuthenticationRequest.class))).thenReturn(authenticationResponse);

        mockMvc.perform(post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authenticationRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("dummy.jwt.token"));
    }

    @Test
    void validateToken_shouldReturnUserDetails_whenTokenIsValid() throws Exception {
        when(authenticationService.validateTokenAndExtractUserDetails(anyString())).thenReturn(userValidationResponse);

        mockMvc.perform(post("/api/v1/auth/validate")
                        .header("Authorization", "Bearer valid.token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("test@example.com"))
                .andExpect(jsonPath("$.authorities[0]").value("ROLE_USER"));
    }

    @Test
    void validateToken_shouldReturnBadRequest_whenAuthHeaderIsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/auth/validate"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Authorization header is missing or not Bearer type."));
    }

    @Test
    void validateToken_shouldReturnBadRequest_whenAuthHeaderIsNotBearer() throws Exception {
        mockMvc.perform(post("/api/v1/auth/validate")
                        .header("Authorization", "Basic somecredentials"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Authorization header is missing or not Bearer type."));
    }

    @Test
    void validateToken_shouldReturnUnauthorized_whenTokenIsInvalid() throws Exception {
        when(authenticationService.validateTokenAndExtractUserDetails(anyString()))
                .thenThrow(new JwtAuthenticationException("Invalid token"));

        mockMvc.perform(post("/api/v1/auth/validate")
                        .header("Authorization", "Bearer invalid.token"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid or expired token: Invalid token"));
    }

    @Test
    void validateToken_shouldReturnInternalServerError_whenServiceThrowsUnexpectedException() throws Exception {
        when(authenticationService.validateTokenAndExtractUserDetails(anyString()))
                .thenThrow(new RuntimeException("Unexpected service error"));

        mockMvc.perform(post("/api/v1/auth/validate")
                        .header("Authorization", "Bearer some.token"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Token validation failed: Unexpected service error"));
    }
}
