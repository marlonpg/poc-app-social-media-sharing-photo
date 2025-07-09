package com.gamba.software.photoapp.filters;

// Removed JwtService and specific JWT exception imports
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;
import com.gamba.software.photoapp.filters.dto.AuthenticationServiceUserResponse; // Added import
import com.gamba.software.photoapp.filters.dto.TokenValidationRequest; // Added import

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final RestTemplate restTemplate;

    @Value("${auth.service.validate.url}") // Configure this in application.properties
    private String authServiceValidateUrl;


    public JwtAuthenticationFilter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
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

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                // Assuming auth-service expects a JSON with the token
                TokenValidationRequest validationRequest = new TokenValidationRequest(jwt);
                HttpEntity<TokenValidationRequest> entity = new HttpEntity<>(validationRequest, headers);

                //ResponseEntity<AuthenticationServiceUserResponse> authResponse = restTemplate.postForEntity(authServiceValidateUrl, entity, AuthenticationServiceUserResponse.class);
                // Using exchange to be more explicit with method
                 ResponseEntity<AuthenticationServiceUserResponse> authResponse = restTemplate.exchange(
                    authServiceValidateUrl,
                    HttpMethod.POST,
                    entity,
                    AuthenticationServiceUserResponse.class
                );


                if (authResponse.getStatusCode().is2xxSuccessful() && authResponse.getBody() != null) {
                    AuthenticationServiceUserResponse userResponse = authResponse.getBody();

                    List<GrantedAuthority> authorities = userResponse.getAuthorities().stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

                    // The principal should be a UserDetails object.
                    // PhotoController expects user.getUsername() to be a parsable UUID string.
                    // So, userResponse.getUserId() should be this UUID string.
                    UserDetails userDetails = new User(userResponse.getUserId(), "", authorities);

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            authorities
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.info("Successfully authenticated user {} via auth-service", userResponse.getUsername());
                } else {
                    logger.warn("Token validation failed with status: {} from auth-service for token (ending): ...{}", authResponse.getStatusCode(), jwt.substring(Math.max(0, jwt.length() - 6)));
                }
            } catch (HttpClientErrorException e) {
                logger.warn("HttpClientErrorException during token validation with auth-service for token (ending): ...{}. Status: {}, Body: {}", jwt.substring(Math.max(0, jwt.length() - 6)), e.getStatusCode(), e.getResponseBodyAsString());
                 // Let Spring Security handle the unauthorized access by not setting authentication
            } catch (Exception e) {
                logger.error("Unexpected error during token validation with auth-service for token (ending): ...{}", jwt.substring(Math.max(0, jwt.length() - 6)), e);
                // Let Spring Security handle
            }
        }

        filterChain.doFilter(request, response);
    }
}
