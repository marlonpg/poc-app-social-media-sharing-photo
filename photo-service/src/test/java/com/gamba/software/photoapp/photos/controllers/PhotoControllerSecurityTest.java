package com.gamba.software.photoapp.photos.controllers;

import com.gamba.software.photoapp.photos.configs.PhotoUserDetailsService;
import com.gamba.software.photoapp.photos.configs.SecurityConfig;
import com.gamba.software.photoapp.photos.filters.JwtAuthenticationFilter;
import com.gamba.software.photoapp.photos.services.PhotoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;
import org.springframework.security.core.userdetails.UserDetailsService;


import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PhotoController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class}) // Import necessary security configurations
class PhotoControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PhotoService photoService; // Mock the service dependency of PhotoController

    // Mock PhotoUserDetailsService (which is the UserDetailsService implementation)
    @MockBean
    @org.springframework.beans.factory.annotation.Qualifier("photoUserDetailsService")
    private UserDetailsService photoUserDetailsService;

    // Mock RestTemplate as it's a dependency for PhotoUserDetailsService which is part of security setup
    @MockBean
    private RestTemplate restTemplate;


    @Test
    void getPhotos_unauthenticated_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/photos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getPhotoById_unauthenticated_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/photos/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    // Test with a mock user provided by UserDetailsService (simulating token validation)
    @Test
    void getPhotos_authenticated_shouldReturnOk() throws Exception {
        // Simulate that PhotoUserDetailsService successfully validates a token
        // The "username" here is the token string passed to loadUserByUsername
        String mockToken = "valid-token-string";
        User mockUserDetails = new User("testuser", "", Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        when(photoUserDetailsService.loadUserByUsername(mockToken)).thenReturn(mockUserDetails);

        mockMvc.perform(get("/api/v1/photos")
                        .header("Authorization", "Bearer " + mockToken))
                .andExpect(status().isOk());
    }

    @Test
    void getPhotoById_authenticated_shouldReturnOk() throws Exception {
        String mockToken = "valid-token-string";
        User mockUserDetails = new User("testuser", "", Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        when(photoUserDetailsService.loadUserByUsername(mockToken)).thenReturn(mockUserDetails);
        UUID photoId = UUID.randomUUID();
        // Assuming photoService.getPhotoById returns a PhotoDto or similar, not mocked here as we only test security.
        // If the controller method requires a return value from photoService to avoid NPE, it should be mocked.
        // For now, focusing on reaching the controller.

        mockMvc.perform(get("/api/v1/photos/" + photoId)
                        .header("Authorization", "Bearer " + mockToken))
                .andExpect(status().isOk()); // Or 404 if photoService returns null and controller handles it
    }

    // Example using @WithMockUser for simplicity if direct token validation is not the focus
    // This bypasses the JwtAuthenticationFilter and PhotoUserDetailsService logic for this specific test method
    @Test
    @WithMockUser(username = "testuser", authorities = {"ROLE_USER"})
    void getPhotos_withMockUser_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/v1/photos"))
                .andExpect(status().isOk());
    }
}
