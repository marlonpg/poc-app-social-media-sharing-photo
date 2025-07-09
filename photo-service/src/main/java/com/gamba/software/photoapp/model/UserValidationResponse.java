package com.gamba.software.photoapp.model;

import java.util.Collection;

// This DTO should mirror the one in auth-service for deserialization
public record UserValidationResponse(String username, Collection<String> authorities) {
}
