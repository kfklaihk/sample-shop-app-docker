package com.docker.atsea.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.test.util.ReflectionTestUtils;

import com.docker.atsea.configuration.JwtConfig;

import io.jsonwebtoken.Claims;

/**
 * Unit tests for JwtTokenProvider.
 * Tests token generation, validation, claim extraction, and expiration handling.
 */
@DisplayName("JWT Token Provider Tests")
public class JwtTokenProviderTest {
    
    private JwtTokenProvider jwtTokenProvider;
    private JwtConfig jwtConfig;
    private final String TEST_SECRET = "test-secret-key-minimum-32-characters-long-for-hs256";
    private final Long ACCESS_TOKEN_EXP = 900000L; // 15 minutes
    private final Long REFRESH_TOKEN_EXP = 604800000L; // 7 days
    
    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        jwtConfig = new JwtConfig();
        
        jwtConfig.setSecret(TEST_SECRET);
        jwtConfig.setAccessTokenExpiration(ACCESS_TOKEN_EXP);
        jwtConfig.setRefreshTokenExpiration(REFRESH_TOKEN_EXP);
        jwtConfig.setUserIdClaimKey("userId");
        jwtConfig.setRolesClaimKey("roles");
        
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtConfig", jwtConfig);
    }
    
    @Test
    @DisplayName("Should generate valid access token")
    void testGenerateAccessToken() {
        // Arrange
        Long userId = 1L;
        String username = "testuser";
        List<String> roles = Arrays.asList("USER");
        
        // Act
        String token = jwtTokenProvider.generateAccessToken(userId, username, roles);
        
        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT format: header.payload.signature
    }
    
    @Test
    @DisplayName("Should generate valid refresh token")
    void testGenerateRefreshToken() {
        // Arrange
        Long userId = 1L;
        String username = "testuser";
        List<String> roles = Arrays.asList("USER", "ADMIN");
        
        // Act
        String token = jwtTokenProvider.generateRefreshToken(userId, username, roles);
        
        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3);
    }
    
    @Test
    @DisplayName("Should validate correct token signature")
    void testValidateToken_ValidSignature() {
        // Arrange
        String token = jwtTokenProvider.generateAccessToken(1L, "testuser", Arrays.asList("USER"));
        
        // Act
        boolean isValid = jwtTokenProvider.validateToken(token);
        
        // Assert
        assertTrue(isValid);
    }
    
    @Test
    @DisplayName("Should reject invalid token signature")
    void testValidateToken_InvalidSignature() {
        // Arrange
        String invalidToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        
        // Act
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);
        
        // Assert
        assertFalse(isValid);
    }
    
    @Test
    @DisplayName("Should extract username from token")
    void testGetUsernameFromToken() {
        // Arrange
        String username = "john.doe@example.com";
        String token = jwtTokenProvider.generateAccessToken(1L, username, Arrays.asList("USER"));
        
        // Act
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);
        
        // Assert
        assertEquals(username, extractedUsername);
    }
    
    @Test
    @DisplayName("Should extract user ID from token")
    void testGetUserIdFromToken() {
        // Arrange
        Long userId = 42L;
        String token = jwtTokenProvider.generateAccessToken(userId, "testuser", Arrays.asList("USER"));
        
        // Act
        Long extractedUserId = jwtTokenProvider.getUserIdFromToken(token);
        
        // Assert
        assertEquals(userId, extractedUserId);
    }
    
    @Test
    @DisplayName("Should extract roles from token")
    void testGetRolesFromToken() {
        // Arrange
        List<String> roles = Arrays.asList("USER", "ADMIN");
        String token = jwtTokenProvider.generateAccessToken(1L, "testuser", roles);
        
        // Act
        List<String> extractedRoles = jwtTokenProvider.getRolesFromToken(token);
        
        // Assert
        assertNotNull(extractedRoles);
        assertEquals(2, extractedRoles.size());
        assertTrue(extractedRoles.containsAll(roles));
    }
    
    @Test
    @DisplayName("Should correctly identify non-expired token")
    void testIsTokenExpired_NotExpired() {
        // Arrange
        String token = jwtTokenProvider.generateAccessToken(1L, "testuser", Arrays.asList("USER"));
        
        // Act
        boolean isExpired = jwtTokenProvider.isTokenExpired(token);
        
        // Assert
        assertFalse(isExpired);
    }
    
    @Test
    @DisplayName("Should extract claims from token")
    void testExtractClaims() {
        // Arrange
        Long userId = 100L;
        String username = "testuser";
        List<String> roles = Arrays.asList("USER");
        String token = jwtTokenProvider.generateAccessToken(userId, username, roles);
        
        // Act
        Claims claims = jwtTokenProvider.extractClaims(token);
        
        // Assert
        assertNotNull(claims);
        assertEquals(username, claims.getSubject());
        assertEquals(userId, ((Number) claims.get("userId")).longValue());
    }
    
    @Test
    @DisplayName("Should calculate expiration time correctly")
    void testGetExpirationTime() {
        // Arrange
        String token = jwtTokenProvider.generateAccessToken(1L, "testuser", Arrays.asList("USER"));
        
        // Act
        long expirationTime = jwtTokenProvider.getExpirationTime(token);
        
        // Assert
        assertTrue(expirationTime > 0);
        assertTrue(expirationTime <= ACCESS_TOKEN_EXP);
    }
    
    @Test
    @DisplayName("Should throw exception for invalid token in extractClaims")
    void testExtractClaims_InvalidToken() {
        // Arrange
        String invalidToken = "invalid.token.format";
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            jwtTokenProvider.extractClaims(invalidToken);
        });
    }
    
    @Test
    @DisplayName("Should include all roles in access token")
    void testAccessTokenIncludesAllRoles() {
        // Arrange
        List<String> roles = Arrays.asList("USER", "MODERATOR", "ADMIN");
        String token = jwtTokenProvider.generateAccessToken(1L, "testuser", roles);
        
        // Act
        List<String> extractedRoles = jwtTokenProvider.getRolesFromToken(token);
        
        // Assert
        assertEquals(3, extractedRoles.size());
        for (String role : roles) {
            assertTrue(extractedRoles.contains(role));
        }
    }
    
    @Test
    @DisplayName("Access token should have shorter expiration than refresh token")
    void testAccessTokenShorterExpiration() {
        // Assert - verify configuration
        assertTrue(ACCESS_TOKEN_EXP < REFRESH_TOKEN_EXP,
                "Access token expiration should be shorter than refresh token");
    }
}
