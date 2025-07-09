package com.gamba.software.photoapp.filters;

import com.gamba.software.photoapp.filters.dto.AuthenticationServiceUserResponse;
import com.gamba.software.photoapp.filters.dto.TokenValidationRequest;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;


import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private RestTemplate restTemplate;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;

    @Captor
    private ArgumentCaptor<HttpEntity<TokenValidationRequest>> httpEntityCaptor;

    private JwtAuthenticationFilter jwtAuthenticationFilter;
    private final String MOCK_AUTH_VALIDATE_URL = "http://auth-service/api/v1/auth/validate-token";


    @BeforeEach
    void setUp() {
        jwtAuthenticationFilter = new JwtAuthenticationFilter(restTemplate);
        ReflectionTestUtils.setField(jwtAuthenticationFilter, "authServiceValidateUrl", MOCK_AUTH_VALIDATE_URL);
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_noAuthorizationHeader_continuesFilterChain() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(restTemplate);
    }

    @Test
    void doFilterInternal_authHeaderNotBearer_continuesFilterChain() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Basic somecredentials");
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(restTemplate);
    }

    @Test
    void doFilterInternal_validBearerToken_authenticatesUser() throws ServletException, IOException {
        String token = "valid.jwt.token";
        String userId = UUID.randomUUID().toString();
        String username = "testuser@example.com";

        AuthenticationServiceUserResponse authUserResponse = new AuthenticationServiceUserResponse();
        authUserResponse.setUserId(userId);
        authUserResponse.setUsername(username);
        authUserResponse.setAuthorities(Arrays.asList("ROLE_USER", "ROLE_VIEWER"));

        ResponseEntity<AuthenticationServiceUserResponse> responseEntity = new ResponseEntity<>(authUserResponse, HttpStatus.OK);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(restTemplate.exchange(
                eq(MOCK_AUTH_VALIDATE_URL),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(AuthenticationServiceUserResponse.class)))
                .thenReturn(responseEntity);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        UserDetails authenticatedUserDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        assertEquals(userId, authenticatedUserDetails.getUsername()); // Principal's username is set to userId from auth response
        assertTrue(authenticatedUserDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
        assertTrue(authenticatedUserDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_VIEWER")));

        verify(restTemplate).exchange(eq(MOCK_AUTH_VALIDATE_URL), eq(HttpMethod.POST), httpEntityCaptor.capture(), eq(AuthenticationServiceUserResponse.class));
        assertEquals(token, httpEntityCaptor.getValue().getBody().getToken());
    }

    @Test
    void doFilterInternal_authServiceReturnsError_doesNotAuthenticate() throws ServletException, IOException {
        String token = "token.for.auth.error";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(restTemplate.exchange(
                eq(MOCK_AUTH_VALIDATE_URL),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(AuthenticationServiceUserResponse.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Token invalid"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_authServiceReturnsNullBody_doesNotAuthenticate() throws ServletException, IOException {
        String token = "token.for.null.body";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        ResponseEntity<AuthenticationServiceUserResponse> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(MOCK_AUTH_VALIDATE_URL),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(AuthenticationServiceUserResponse.class)))
                .thenReturn(responseEntity);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }


    @Test
    void doFilterInternal_userAlreadyAuthenticated_doesNotCallAuthService() throws ServletException, IOException {
        UserDetails userDetails = new User("testuser", "", Collections.emptyList());
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(request.getHeader("Authorization")).thenReturn("Bearer some.token");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(restTemplate);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
