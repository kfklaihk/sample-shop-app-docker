package com.docker.atsea.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for token refresh requests.
 * Contains the refresh token to obtain a new access token.
 */
public class RefreshTokenRequest {
    
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
    
    public RefreshTokenRequest() {
    }
    
    public RefreshTokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    // Getters and Setters
    
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
