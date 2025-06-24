package com.gamba.software.photoapp.auth.exceptions;

public class JwtAuthenticationException extends RuntimeException {
    public JwtAuthenticationException(String message) {
        super(message);
    }
    public JwtAuthenticationException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
