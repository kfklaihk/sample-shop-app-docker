package com.docker.atsea.security;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.docker.atsea.configuration.JwtConfig;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;

/**
 * JWT Token Provider for creating, validating, and extracting claims from JWT tokens.
 * Supports both access tokens (short-lived) and refresh tokens (long-lived).
 * 
 * Token Claims:
 * - userId: The customer ID (subject)
 * - roles: List of user roles for authorization
 * - iat: Issued at timestamp
 * - exp: Expiration timestamp
 */
@Component
public class JwtTokenProvider {
    
    @Autowired
    private JwtConfig jwtConfig;
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes());
    }
    
    /**
     * Generate an access token for a user.
     * Access tokens are short-lived (default 15 minutes).
     * 
     * @param userId the user ID (customer ID)
     * @param username the username
     * @param roles list of user roles
     * @return JWT access token string
     */
    public String generateAccessToken(Long userId, String username, List<String> roles) {
        return generateToken(userId, username, roles, jwtConfig.getAccessTokenExpiration());
    }
    
    /**
     * Generate a refresh token for a user.
     * Refresh tokens are long-lived (default 7 days).
     * Used to obtain new access tokens without re-authentication.
     * 
     * @param userId the user ID (customer ID)
     * @param username the username
     * @param roles list of user roles
     * @return JWT refresh token string
     */
    public String generateRefreshToken(Long userId, String username, List<String> roles) {
        return generateToken(userId, username, roles, jwtConfig.getRefreshTokenExpiration());
    }
    
    /**
     * Generate a JWT token with specified expiration.
     * 
     * @param userId the user ID
     * @param username the username (used as subject)
     * @param roles list of user roles
     * @param expirationMs expiration time in milliseconds from now
     * @return JWT token string
     */
    private String generateToken(Long userId, String username, List<String> roles, Long expirationMs) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(jwtConfig.getUserIdClaimKey(), userId);
        claims.put(jwtConfig.getRolesClaimKey(), roles);
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * Validate JWT token signature and expiration.
     * 
     * @param token the JWT token string
     * @return true if token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            // Token is invalid (signature mismatch, expired, malformed, etc.)
            return false;
        }
    }
    
    /**
     * Extract all claims from a JWT token.
     * 
     * @param token the JWT token string
     * @return Claims object containing all token claims
     * @throws IllegalArgumentException if token is invalid
     */
    public Claims extractClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid token: " + e.getMessage());
        }
    }
    
    /**
     * Extract username (subject) from token.
     * 
     * @param token the JWT token string
     * @return username
     */
    public String getUsernameFromToken(String token) {
        return extractClaims(token).getSubject();
    }
    
    /**
     * Extract user ID from token claims.
     * 
     * @param token the JWT token string
     * @return user ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = extractClaims(token);
        Object userIdObj = claims.get(jwtConfig.getUserIdClaimKey());
        if (userIdObj instanceof Number) {
            return ((Number) userIdObj).longValue();
        }
        return null;
    }
    
    /**
     * Extract roles from token claims.
     * 
     * @param token the JWT token string
     * @return list of roles
     */
    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        Claims claims = extractClaims(token);
        return (List<String>) claims.get(jwtConfig.getRolesClaimKey());
    }
    
    /**
     * Check if token is expired.
     * 
     * @param token the JWT token string
     * @return true if expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = extractClaims(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
    
    /**
     * Get remaining expiration time for token.
     * 
     * @param token the JWT token string
     * @return milliseconds until expiration, or -1 if expired/invalid
     */
    public long getExpirationTime(String token) {
        try {
            Claims claims = extractClaims(token);
            Date expirationDate = claims.getExpiration();
            long remainingMs = expirationDate.getTime() - System.currentTimeMillis();
            return remainingMs > 0 ? remainingMs : -1;
        } catch (Exception e) {
            return -1;
        }
    }
}
