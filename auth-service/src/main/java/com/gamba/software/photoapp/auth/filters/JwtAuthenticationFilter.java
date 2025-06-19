package com.gamba.software.photoapp.auth.filters;

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

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
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

        // Skip filter for whitelisted endpoints like /api/v1/auth/**
        if (request.getServletPath().startsWith("/api/v1/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // If no token, continue to the next filter.
            // If the endpoint is secured, Spring Security will handle it.
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
                            null, // Credentials are not needed as token is validated
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.info("Successfully authenticated user: {}", userEmail);
                } else {
                    // This case might be rare if isTokenValid throws exceptions for most invalid scenarios
                    logger.warn("Invalid JWT token for user: {}. Token validation returned false.", userEmail);
                }
            }
        } catch (MalformedJwtException e) {
            logger.warn("JWT token processing failed: Malformed JWT. Token: [{}], Error: {}", jwt, e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.warn("JWT token processing failed: Expired JWT. Token: [{}], User: [{}], Error: {}", jwt, e.getClaims().getSubject(), e.getMessage());
        } catch (SignatureException e) {
            logger.warn("JWT token processing failed: Invalid Signature. Token: [{}], Error: {}", jwt, e.getMessage());
        } catch (JwtException e) { // Catch other JWT-related exceptions
            logger.warn("JWT token processing failed: Generic JWT Exception. Token: [{}], Error: {}", jwt, e.getMessage());
        } catch (UsernameNotFoundException e) {
            logger.warn("JWT token processing failed: User not found for token. Extracted email may be invalid. Error: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during JWT authentication filter processing for request: {}", request.getRequestURI(), e);
        }

        filterChain.doFilter(request, response);
    }
}
