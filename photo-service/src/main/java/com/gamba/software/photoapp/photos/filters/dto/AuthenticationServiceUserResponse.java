package com.gamba.software.photoapp.photos.filters.dto;

import java.util.List;

public class AuthenticationServiceUserResponse {
    private String userId;
    private String username;
    private List<String> authorities;

    // Default constructor for JSON deserialization
    public AuthenticationServiceUserResponse() {
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public List<String> getAuthorities() { return authorities; }
    public void setAuthorities(List<String> authorities) { this.authorities = authorities; }
}
