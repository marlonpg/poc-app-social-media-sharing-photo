package com.gamba.software.photoapp.photos.filters;

import com.gamba.software.photoapp.shared.jwt.JwtService; // Corrected import
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier; // For UserDetailsService
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService; // Will be photo-service's JwtService
    private final UserDetailsService userDetailsService; // Will be photo-service's PhotoUserDetailsService

    // Updated constructor to inject photo-service specific UserDetailsService
    public JwtAuthenticationFilter(JwtService jwtService, @Qualifier("photoUserDetailsService") UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // Removed path skipping logic for /api/v1/auth/** as it's not relevant for photo-service
        // The filter will now attempt to validate a token on all paths it is applied to.

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);

        try {
            userEmail = jwtService.extractUsername(jwt);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.info("Successfully authenticated user (in photo-service): {}", userEmail);
                } else {
                    logger.warn("Invalid JWT token for user (in photo-service): {}. Token validation returned false.", userEmail);
                }
            }
        } catch (MalformedJwtException e) {
            logger.warn("JWT token processing failed (in photo-service): Malformed JWT. Token: [{}], Error: {}", jwt, e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.warn("JWT token processing failed (in photo-service): Expired JWT. Token: [{}], User: [{}], Error: {}", jwt, e.getClaims().getSubject(), e.getMessage());
        } catch (SignatureException e) {
            logger.warn("JWT token processing failed (in photo-service): Invalid Signature. Token: [{}], Error: {}", jwt, e.getMessage());
        } catch (JwtException e) {
            logger.warn("JWT token processing failed (in photo-service): Generic JWT Exception. Token: [{}], Error: {}", jwt, e.getMessage());
        } catch (UsernameNotFoundException e) {
            logger.warn("JWT token processing failed (in photo-service): User not found by PhotoUserDetailsService. Error: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during JWT authentication filter processing (in photo-service) for request: {}", request.getRequestURI(), e);
        }

        filterChain.doFilter(request, response);
    }
}
