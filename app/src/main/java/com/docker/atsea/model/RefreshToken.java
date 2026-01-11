package com.docker.atsea.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

/**
 * RefreshToken entity for managing JWT refresh tokens.
 * Used for token rotation and logout (blacklisting).
 * 
 * When a user logs out, their refresh token is marked as revoked.
 * The refresh token endpoint checks this status before issuing new access tokens.
 */
@Entity
@Table(name = "refresh_token")
public class RefreshToken implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id")
    private Long tokenId;
    
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    @Column(name = "token", nullable = false, length = 500, unique = true)
    private String token;
    
    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;
    
    @Column(name = "revoked", nullable = false)
    private Boolean revoked = false; // For logout blacklisting
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public RefreshToken() {
    }
    
    public RefreshToken(Customer customer, String token, LocalDateTime expiryDate) {
        this.customer = customer;
        this.token = token;
        this.expiryDate = expiryDate;
        this.revoked = false;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    
    public Long getTokenId() {
        return tokenId;
    }
    
    public void setTokenId(Long tokenId) {
        this.tokenId = tokenId;
    }
    
    public Customer getCustomer() {
        return customer;
    }
    
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }
    
    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }
    
    public Boolean getRevoked() {
        return revoked;
    }
    
    public void setRevoked(Boolean revoked) {
        this.revoked = revoked;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    /**
     * Check if token is valid (not expired and not revoked).
     */
    public boolean isValid() {
        return !revoked && LocalDateTime.now().isBefore(expiryDate);
    }
    
    @Override
    public String toString() {
        return "RefreshToken [tokenId=" + tokenId + ", customerId=" + customer.getCustomerId()
                + ", revoked=" + revoked + ", createdAt=" + createdAt + "]";
    }
}
