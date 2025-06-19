package com.gamba.software.photoapp.photos.configs;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList; // For authorities list

/**
 * Stub UserDetailsService for PhotoService.
 * It creates UserDetails from the username extracted from the JWT.
 * Authorities might be extracted from the JWT in a future enhancement.
 */
@Service("photoUserDetailsService") // Give it a specific name if default UserDetailsService might conflict
public class PhotoUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username == null || username.isEmpty()) {
            throw new UsernameNotFoundException("Username cannot be empty");
        }

        // For now, password is not used for JWT validation, authorities can be empty or derived from JWT.
        // Roles/authorities can be parsed from JWT claims if available.
        // Example: List<GrantedAuthority> authorities = extractAuthoritiesFromJwtClaims(jwtService.extractAllClaims(token));
        return new User(username, "", new ArrayList<>()); // Empty password, empty authorities
    }
}
