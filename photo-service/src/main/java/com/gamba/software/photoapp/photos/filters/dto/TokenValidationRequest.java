package com.gamba.software.photoapp.photos.filters.dto;

public class TokenValidationRequest {
    private String token;

    // Default constructor for JSON deserialization (if ever needed, though likely not for request)
    public TokenValidationRequest() {
    }

    public TokenValidationRequest(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
