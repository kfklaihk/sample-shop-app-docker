package com.docker.atsea.repositories;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.docker.atsea.model.RefreshToken;
import com.docker.atsea.model.Customer;

/**
 * Repository for RefreshToken entity.
 * Manages token persistence, expiration, and revocation queries.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    
    /**
     * Find refresh token by token string.
     * Used for token validation in refresh endpoint.
     */
    Optional<RefreshToken> findByToken(String token);
    
    /**
     * Find all refresh tokens for a customer.
     * Used for logout (revoke all tokens).
     */
    java.util.List<RefreshToken> findByCustomer(Customer customer);
    
    /**
     * Check if refresh token exists by token string.
     */
    boolean existsByToken(String token);
    
    /**
     * Delete expired tokens (cleanup job).
     * Run periodically to clean up old expired tokens.
     */
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < :currentTime")
    void deleteExpiredTokens(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Find all revoked tokens for a customer.
     * Used for audit and verification.
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.customer = :customer AND rt.revoked = true")
    java.util.List<RefreshToken> findRevokedTokensByCustomer(@Param("customer") Customer customer);
}
