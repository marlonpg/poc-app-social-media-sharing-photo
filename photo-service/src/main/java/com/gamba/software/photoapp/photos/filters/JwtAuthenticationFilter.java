package com.gamba.software.photoapp.photos.filters;

// Removed import com.gamba.software.photoapp.shared.jwt.JwtService;
// Removed JWT specific exception imports as validation is delegated
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
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

    // Removed JwtService
    private final UserDetailsService userDetailsService; // Will be photo-service's PhotoUserDetailsService

    public JwtAuthenticationFilter(@Qualifier("photoUserDetailsService") UserDetailsService userDetailsService) {
        // Removed JwtService from constructor
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);

        try {
            // The PhotoUserDetailsService is now responsible for validating the token (jwt string)
            // and returning UserDetails if successful.
            // It internally calls the auth-service.
            // If SecurityContextHolder.getContext().getAuthentication() is already set,
            // it means a previous filter in the chain (or a cached security context)
            // has already authenticated the user for this request.
            // We should only proceed if there's no existing authentication.
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(jwt); // Pass the token itself

                // If loadUserByUsername returns (i.e., doesn't throw UsernameNotFoundException),
                // it means auth-service considered the token valid and returned user details.
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null, // Credentials not needed as token is validated by auth-service
                        userDetails.getAuthorities()
                );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
                logger.info("Successfully authenticated user (via auth-service): {}", userDetails.getUsername());
            }
        } catch (UsernameNotFoundException e) {
            // This exception is thrown by PhotoUserDetailsService if auth-service validation fails
            logger.warn("Token validation failed via auth-service or user not found: {}", e.getMessage());
            // Optionally, clear context if partially set, though typically not needed here
            // SecurityContextHolder.clearContext();
            // Depending on requirements, you might want to send a 401 response here,
            // but Spring Security's ExceptionTranslationFilter usually handles that
            // if the request reaches a secured endpoint without successful authentication.
        } catch (Exception e) {
            // Catch-all for any other unexpected errors during the process
            logger.error("Unexpected error during authentication filter processing in photo-service for request: {}", request.getRequestURI(), e);
            // SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
