package com.gamba.software.photoapp.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class ResourceAccessDeniedException extends RuntimeException {
    public ResourceAccessDeniedException(String resourceName, Object identifier) {
        super(String.format("Access denied for %s with ID: %s", resourceName, identifier));
    }
}
