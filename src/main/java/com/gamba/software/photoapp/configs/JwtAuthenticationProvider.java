package com.gamba.software.photoapp.configs;

import com.gamba.software.photoapp.exceptions.JwtAuthenticationException;
import com.gamba.software.photoapp.services.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthenticationProvider implements AuthenticationProvider {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationProvider(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        final String jwt = (String) authentication.getCredentials();

        if (jwt == null || jwt.isBlank()) {
            // This specific check can remain as JwtAuthenticationException or be mapped to MalformedJwtException
            // For now, let's keep it as is, assuming it's a pre-parsing validation
            throw new JwtAuthenticationException("Empty or null JWT");
        }

        try {
            final String username = jwtService.extractUsername(jwt);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtService.isTokenValid(jwt, userDetails)) {
                return new UsernamePasswordAuthenticationToken(
                        userDetails,
                        jwt,
                        userDetails.getAuthorities()
                );
            }
            // If isTokenValid returns false for reasons other than specific JWT exceptions (e.g. user not found, account locked)
            // it might still throw a generic exception or the service method itself should throw something more specific.
            // For now, if it returns false without throwing, we'll assume a generic invalid token.
            throw new JwtAuthenticationException("Invalid JWT token");
        } catch (MalformedJwtException e) {
            throw new JwtAuthenticationException("Malformed JWT: " + e.getMessage(), e);
        } catch (ExpiredJwtException e) {
            throw new JwtAuthenticationException("Expired JWT: " + e.getMessage(), e);
        } catch (SignatureException e) {
            throw new JwtAuthenticationException("JWT signature validation failed: " + e.getMessage(), e);
        }
        // Catching a broader JwtException from io.jsonwebtoken if there are other unhandled cases
        // or relying on JwtService to throw specific exceptions that are then caught here.
        // For now, let's assume other specific exceptions from jwtService if any would be caught by their own type
        // or a more general AuthenticationException by Spring Security framework.
    }


    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}