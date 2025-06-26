package com.gamba.software.photoapp.auth.controllers;

import com.gamba.software.photoapp.auth.controllers.dto.AuthenticationRequest;
import com.gamba.software.photoapp.auth.controllers.dto.AuthenticationResponse;
import com.gamba.software.photoapp.auth.controllers.dto.RegisterRequest;
import com.gamba.software.photoapp.auth.controllers.dto.UserValidationResponse;
import com.gamba.software.photoapp.auth.services.AuthenticationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("Authorization header is missing or not Bearer type.");
        }
        String token = authHeader.substring(7);
        try {
            UserValidationResponse response = authenticationService.validateTokenAndExtractUserDetails(token);
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            // More specific exceptions can be caught here if needed, e.g., ExpiredJwtException
            return ResponseEntity.status(401).body("Invalid or expired token: " + e.getMessage());
        } catch (Exception e) {
            // Generic error handler
            return ResponseEntity.status(500).body("Token validation failed: " + e.getMessage());
        }
    }
}
