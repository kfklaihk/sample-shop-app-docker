package com.docker.atsea.configuration;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

/**
 * Unit tests for JwtConfig.
 * Tests configuration property loading and default values.
 */
@DisplayName("JWT Configuration Tests")
public class JwtConfigTest {
    
    private JwtConfig jwtConfig;
    
    @BeforeEach
    void setUp() {
        jwtConfig = new JwtConfig();
    }
    
    @Test
    @DisplayName("Should set and get JWT secret")
    void testSetAndGetSecret() {
        // Arrange
        String secret = "my-super-secret-key-minimum-32-characters-long";
        
        // Act
        jwtConfig.setSecret(secret);
        
        // Assert
        assertEquals(secret, jwtConfig.getSecret());
    }
    
    @Test
    @DisplayName("Should set and get access token expiration")
    void testSetAndGetAccessTokenExpiration() {
        // Arrange
        Long expiration = 900000L; // 15 minutes
        
        // Act
        jwtConfig.setAccessTokenExpiration(expiration);
        
        // Assert
        assertEquals(expiration, jwtConfig.getAccessTokenExpiration());
    }
    
    @Test
    @DisplayName("Should set and get refresh token expiration")
    void testSetAndGetRefreshTokenExpiration() {
        // Arrange
        Long expiration = 604800000L; // 7 days
        
        // Act
        jwtConfig.setRefreshTokenExpiration(expiration);
        
        // Assert
        assertEquals(expiration, jwtConfig.getRefreshTokenExpiration());
    }
    
    @Test
    @DisplayName("Should have default access token expiration of 15 minutes")
    void testDefaultAccessTokenExpiration() {
        // Assert
        assertEquals(900000L, jwtConfig.getAccessTokenExpiration());
    }
    
    @Test
    @DisplayName("Should have default refresh token expiration of 7 days")
    void testDefaultRefreshTokenExpiration() {
        // Assert
        assertEquals(604800000L, jwtConfig.getRefreshTokenExpiration());
    }
    
    @Test
    @DisplayName("Should set and get user ID claim key")
    void testSetAndGetUserIdClaimKey() {
        // Arrange
        String key = "customUserId";
        
        // Act
        jwtConfig.setUserIdClaimKey(key);
        
        // Assert
        assertEquals(key, jwtConfig.getUserIdClaimKey());
    }
    
    @Test
    @DisplayName("Should set and get roles claim key")
    void testSetAndGetRolesClaimKey() {
        // Arrange
        String key = "customRoles";
        
        // Act
        jwtConfig.setRolesClaimKey(key);
        
        // Assert
        assertEquals(key, jwtConfig.getRolesClaimKey());
    }
    
    @Test
    @DisplayName("Should have default user ID claim key")
    void testDefaultUserIdClaimKey() {
        // Assert
        assertEquals("userId", jwtConfig.getUserIdClaimKey());
    }
    
    @Test
    @DisplayName("Should have default roles claim key")
    void testDefaultRolesClaimKey() {
        // Assert
        assertEquals("roles", jwtConfig.getRolesClaimKey());
    }
    
    @Test
    @DisplayName("Access token expiration should be less than refresh token expiration")
    void testExpirationHierarchy() {
        // Assert
        assertTrue(jwtConfig.getAccessTokenExpiration() < jwtConfig.getRefreshTokenExpiration(),
                "Access token expiration should be shorter than refresh token expiration");
    }
}
