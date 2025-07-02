package com.gamba.software.photoapp.photos.controllers;

import com.gamba.software.photoapp.photos.configs.SecurityConfig;
import com.gamba.software.photoapp.photos.filters.JwtAuthenticationFilter;
import com.gamba.software.photoapp.photos.filters.dto.AuthenticationServiceUserResponse;
import com.gamba.software.photoapp.photos.services.PhotoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate; // Import RestTemplate

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString; // Added import
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post; // Added post import
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PhotoController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class PhotoControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PhotoService photoService;

    @MockBean
    private RestTemplate restTemplate; // Mock RestTemplate

    @Value("${auth.service.validate.url}")
    private String authServiceValidateUrl;


    @Test
    void getPhotoById_unauthenticated_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/photos/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getPhotoById_authenticated_viaApiCall_shouldReturnOk() throws Exception {
        String mockToken = "valid-token-string";
        String mockUserIdString = UUID.randomUUID().toString();
        String mockUsername = "testuser@example.com";

        AuthenticationServiceUserResponse authUserResponse = new AuthenticationServiceUserResponse();
        authUserResponse.setUserId(mockUserIdString);
        authUserResponse.setUsername(mockUsername);
        authUserResponse.setAuthorities(Collections.singletonList("ROLE_USER"));

        ResponseEntity<AuthenticationServiceUserResponse> responseEntity =
                new ResponseEntity<>(authUserResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(authServiceValidateUrl), // Use the injected value
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(AuthenticationServiceUserResponse.class)))
                .thenReturn(responseEntity);

        mockMvc.perform(get("/api/v1/photos/" + UUID.randomUUID())
                        .header("Authorization", "Bearer " + mockToken))
                .andExpect(status().isOk());
    }

    @Test
    void publishPhoto_authenticated_viaApiCall_shouldReturnOk() throws Exception {
        String mockToken = "valid-token-string-for-publish";
        String mockUserIdString = UUID.randomUUID().toString(); // This needs to be a parsable UUID for the controller
        String mockUsername = "publisher@example.com";

        AuthenticationServiceUserResponse authUserResponse = new AuthenticationServiceUserResponse();
        authUserResponse.setUserId(mockUserIdString); // This will be used as principal name
        authUserResponse.setUsername(mockUsername);
        authUserResponse.setAuthorities(Arrays.asList("ROLE_USER"));

        ResponseEntity<AuthenticationServiceUserResponse> responseEntity =
                new ResponseEntity<>(authUserResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(authServiceValidateUrl),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(AuthenticationServiceUserResponse.class)))
                .thenReturn(responseEntity);

        // Mock photoService call for publishPhoto as it's called by the controller
        // This is not strictly for security testing but to make the controller method complete without NPE.
        // What PhotoResponse to return depends on the PhotoService mock setup, can be null if not checked.
        when(photoService.publishPhoto(eq(UUID.fromString(mockUserIdString)), anyString(), anyString(), any()))
              .thenReturn(null); // Or a mocked PhotoResponse


        mockMvc.perform(post("/api/v1/photos/publish")
                        .header("Authorization", "Bearer " + mockToken)
                        .contentType("application/json")
                        .content("{\"caption\":\"Test Caption\", \"imageUrl\":\"http://example.com/image.jpg\", \"privacy\":\"PUBLIC\"}"))
                .andExpect(status().isOk());
    }


    @Test
    @WithMockUser(username = "00000000-0000-0000-0000-000000000000", authorities = {"ROLE_USER"})
    void publishPhoto_withMockUser_shouldReturnOk() throws Exception {
        when(photoService.publishPhoto(eq(UUID.fromString("00000000-0000-0000-0000-000000000000")), anyString(), anyString(), any()))
              .thenReturn(null); // Or a mocked PhotoResponse

        mockMvc.perform(post("/api/v1/photos/publish")
                        .contentType("application/json")
                        .content("{\"caption\":\"Test Caption\", \"imageUrl\":\"http://example.com/image.jpg\", \"privacy\":\"PUBLIC\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "11111111-1111-1111-1111-111111111111", authorities = {"ROLE_USER"})
    void getPhotoById_withMockUser_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/v1/photos/" + UUID.randomUUID()))
                .andExpect(status().isOk());
    }
}
