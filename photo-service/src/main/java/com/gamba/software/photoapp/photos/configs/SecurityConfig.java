package com.gamba.software.photoapp.photos.configs;

import com.gamba.software.photoapp.photos.filters.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder; // For DaoAuthenticationProvider, though not strictly used with JWT directly
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // Example encoder
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService photoUserDetailsService; // Injected: PhotoUserDetailsService

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter,
                          @Qualifier("photoUserDetailsService") UserDetailsService photoUserDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.photoUserDetailsService = photoUserDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Example: Allow public access to GET photos, but require auth for POST/PUT/DELETE
                        // .requestMatchers(HttpMethod.GET, "/api/v1/photos/**").permitAll()
                        // .requestMatchers("/api/v1/photos/upload").authenticated() // Specific example from auth-service
                        .requestMatchers("/api/v1/photos/**").authenticated() // General rule for photos
                        .anyRequest().authenticated() // Default deny for anything else
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // The AuthenticationProvider will be automatically configured if UserDetailsService is present
                // Or we can define one explicitly:
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(photoUserDetailsService);
        // PasswordEncoder is required by DaoAuthenticationProvider, even if we don't check passwords for JWTs.
        // The password from UserDetails stub is empty, so this won't be used for matching.
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Used by DaoAuthenticationProvider. Not directly for JWT validation itself.
        return new BCryptPasswordEncoder();
    }
}
