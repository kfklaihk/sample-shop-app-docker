package com.docker.atsea.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * JWT configuration properties loaded from application.yml.
 * Externalizes JWT secret and expiration settings from code.
 * 
 * Configuration format in application.yml:
 * jwt:
 *   secret: your-secret-key-min-32-chars
 *   access-token-expiration: 900000  # 15 minutes in ms
 *   refresh-token-expiration: 604800000  # 7 days in ms
 */
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {
    
    /**
     * JWT secret key for signing tokens.
     * Must be at least 32 characters long for HS256.
     * Example: "my-super-secret-jwt-key-minimum-32-chars"
     */
    private String secret;
    
    /**
     * Access token expiration time in milliseconds.
     * Default: 900000 (15 minutes)
     * Short-lived for security; users refresh tokens when expired.
     */
    private Long accessTokenExpiration = 900000L; // 15 minutes
    
    /**
     * Refresh token expiration time in milliseconds.
     * Default: 604800000 (7 days)
     * Longer-lived to allow users to stay logged in without password re-entry.
     */
    private Long refreshTokenExpiration = 604800000L; // 7 days
    
    /**
     * JWT token claim key for user ID.
     * Used to extract user ID from token claims.
     */
    private String userIdClaimKey = "userId";
    
    /**
     * JWT token claim key for roles.
     * Used to extract user roles from token claims.
     */
    private String rolesClaimKey = "roles";
    
    // Getters and Setters
    
    public String getSecret() {
        return secret;
    }
    
    public void setSecret(String secret) {
        this.secret = secret;
    }
    
    public Long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }
    
    public void setAccessTokenExpiration(Long accessTokenExpiration) {
        this.accessTokenExpiration = accessTokenExpiration;
    }
    
    public Long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }
    
    public void setRefreshTokenExpiration(Long refreshTokenExpiration) {
        this.refreshTokenExpiration = refreshTokenExpiration;
    }
    
    public String getUserIdClaimKey() {
        return userIdClaimKey;
    }
    
    public void setUserIdClaimKey(String userIdClaimKey) {
        this.userIdClaimKey = userIdClaimKey;
    }
    
    public String getRolesClaimKey() {
        return rolesClaimKey;
    }
    
    public void setRolesClaimKey(String rolesClaimKey) {
        this.rolesClaimKey = rolesClaimKey;
    }
}
