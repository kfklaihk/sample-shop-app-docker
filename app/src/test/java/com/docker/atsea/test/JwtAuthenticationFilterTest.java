package com.docker.atsea.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.docker.atsea.configuration.JwtConfig;

/**
 * Unit tests for JwtAuthenticationFilter.
 * Tests JWT extraction from headers, validation, and SecurityContext population.
 */
@DisplayName("JWT Authentication Filter Tests")
public class JwtAuthenticationFilterTest {
    
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    private JwtTokenProvider jwtTokenProvider;
    private JwtConfig jwtConfig;
    private final String TEST_SECRET = "test-secret-key-minimum-32-characters-long-for-hs256";
    
    @BeforeEach
    void setUp() {
        jwtAuthenticationFilter = new JwtAuthenticationFilter();
        jwtTokenProvider = new JwtTokenProvider();
        jwtConfig = new JwtConfig();
        
        jwtConfig.setSecret(TEST_SECRET);
        jwtConfig.setAccessTokenExpiration(900000L);
        jwtConfig.setRefreshTokenExpiration(604800000L);
        jwtConfig.setUserIdClaimKey("userId");
        jwtConfig.setRolesClaimKey("roles");
        
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtConfig", jwtConfig);
        ReflectionTestUtils.setField(jwtAuthenticationFilter, "jwtTokenProvider", jwtTokenProvider);
        
        // Clear SecurityContext before each test
        SecurityContextHolder.clearContext();
    }
    
    @Test
    @DisplayName("Should extract token from Authorization header with Bearer prefix")
    void testExtractToken_ValidFormat() throws Exception {
        // Arrange
        String token = jwtTokenProvider.generateAccessToken(1L, "testuser", Arrays.asList("USER"));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);
        
        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        // Assert - filter should continue chain
        verify(filterChain, times(1)).doFilter(request, response);
    }
    
    @Test
    @DisplayName("Should handle missing Authorization header gracefully")
    void testExtractToken_MissingHeader() throws Exception {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);
        
        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        // Assert - filter should continue without authentication
        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
    
    @Test
    @DisplayName("Should handle invalid Bearer token format")
    void testExtractToken_InvalidBearerFormat() throws Exception {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "InvalidToken");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);
        
        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        // Assert - filter should continue without authentication
        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
    
    @Test
    @DisplayName("Should populate SecurityContext with valid JWT token")
    void testDoFilterInternal_ValidToken() throws Exception {
        // Arrange
        String username = "john.doe@example.com";
        Long userId = 42L;
        List<String> roles = Arrays.asList("USER", "ADMIN");
        String token = jwtTokenProvider.generateAccessToken(userId, username, roles);
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);
        
        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        // Assert
        verify(filterChain, times(1)).doFilter(request, response);
        
        // Verify SecurityContext is populated
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(username, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        assertEquals(2, SecurityContextHolder.getContext().getAuthentication().getAuthorities().size());
    }
    
    @Test
    @DisplayName("Should not populate SecurityContext with invalid JWT signature")
    void testDoFilterInternal_InvalidSignature() throws Exception {
        // Arrange
        String invalidToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0dXNlciIsInVzZXJJZCI6MSwicm9sZXMiOlsiVVNFUiJdfQ.invalidSignature";
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + invalidToken);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);
        
        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        // Assert - filter should continue but without authentication
        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
    
    @Test
    @DisplayName("Should convert roles to Spring Security authorities with ROLE_ prefix")
    void testRoleConversion_ProperPrefix() throws Exception {
        // Arrange
        String token = jwtTokenProvider.generateAccessToken(1L, "testuser", Arrays.asList("USER", "ADMIN"));
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);
        
        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        // Assert
        var authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        assertTrue(authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")),
                "Should contain ROLE_USER authority");
        assertTrue(authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")),
                "Should contain ROLE_ADMIN authority");
    }
    
    @Test
    @DisplayName("Should continue filter chain even on token validation error")
    void testDoFilterInternal_ContinuesOnError() throws Exception {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer malformed.token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);
        
        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> {
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        });
        
        // Verify chain continues
        verify(filterChain, times(1)).doFilter(request, response);
    }
    
    @Test
    @DisplayName("Should set user ID as authentication details")
    void testAuthenticationDetails_UserIdSet() throws Exception {
        // Arrange
        Long userId = 123L;
        String token = jwtTokenProvider.generateAccessToken(userId, "testuser", Arrays.asList("USER"));
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);
        
        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        // Assert
        assertEquals(userId, SecurityContextHolder.getContext().getAuthentication().getDetails());
    }
}
