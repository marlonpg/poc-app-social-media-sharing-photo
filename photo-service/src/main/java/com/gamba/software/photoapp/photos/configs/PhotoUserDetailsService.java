package com.gamba.software.photoapp.photos.configs;

import com.gamba.software.photoapp.photos.model.UserValidationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service("photoUserDetailsService")
public class PhotoUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(PhotoUserDetailsService.class);

    private final RestTemplate restTemplate;
    private final String authServiceUrl;

    public PhotoUserDetailsService(RestTemplate restTemplate,
                                   @Value("${auth.service.url}") String authServiceUrl) {
        this.restTemplate = restTemplate;
        this.authServiceUrl = authServiceUrl;
    }

    /**
     * Loads user details by validating the provided token against the auth-service.
     * The 'username' parameter is expected to be the JWT token itself.
     *
     * @param token the JWT token.
     * @return UserDetails if the token is valid.
     * @throws UsernameNotFoundException if the token is invalid or user cannot be authenticated.
     */
    @Override
    public UserDetails loadUserByUsername(String token) throws UsernameNotFoundException {
        if (token == null || token.isEmpty()) {
            throw new UsernameNotFoundException("Token cannot be empty");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            logger.debug("Validating token with auth-service at URL: {}/validate", authServiceUrl);
            ResponseEntity<UserValidationResponse> response = restTemplate.exchange(
                    authServiceUrl + "/validate",
                    HttpMethod.POST,
                    entity,
                    UserValidationResponse.class
            );

            UserValidationResponse validationResponse = response.getBody();
            if (validationResponse != null && validationResponse.username() != null) {
                List<GrantedAuthority> authorities = validationResponse.authorities() == null ?
                        new ArrayList<>() :
                        validationResponse.authorities().stream()
                                .map(SimpleGrantedAuthority::new)
                                .collect(Collectors.toList());
                logger.info("Successfully validated token for user: {}", validationResponse.username());
                return new User(validationResponse.username(), "", authorities); // Password field is not used here
            } else {
                logger.warn("Auth-service validation response was null or username was null for token: {}", token);
                throw new UsernameNotFoundException("User details not found from token via auth-service.");
            }
        } catch (HttpClientErrorException e) {
            logger.warn("Error validating token with auth-service. Status: {}, Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new UsernameNotFoundException("Failed to validate token with auth-service: " + e.getStatusCode(), e);
        } catch (Exception e) {
            logger.error("Unexpected error during token validation with auth-service for token: {}", token, e);
            throw new UsernameNotFoundException("Unexpected error validating token.", e);
        }
    }
}
