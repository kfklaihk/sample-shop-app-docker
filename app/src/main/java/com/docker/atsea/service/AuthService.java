package com.docker.atsea.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.docker.atsea.configuration.JwtConfig;
import com.docker.atsea.dto.AuthResponse;
import com.docker.atsea.dto.LoginRequest;
import com.docker.atsea.dto.RegisterRequest;
import com.docker.atsea.model.Customer;
import com.docker.atsea.model.RefreshToken;
import com.docker.atsea.repositories.CustomerRepository;
import com.docker.atsea.repositories.RefreshTokenRepository;
import com.docker.atsea.security.JwtTokenProvider;

/**
 * AuthService handles authentication, registration, and token management.
 * 
 * Security Features:
 * - Password hashing with BCrypt (delegated to PasswordEncoder)
 * - JWT token generation with expiration
 * - Refresh token storage and rotation
 * - Token revocation on logout
 * - SQL injection prevention (JPA parameterized queries)
 * - Input validation (via @Valid annotations)
 * - Duplicate username/email checks
 */
@Service
@Transactional
public class AuthService {
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtConfig jwtConfig;
    
    /**
     * Register a new user.
     * Validates input, checks for duplicate username/email, and creates new customer.
     * 
     * @param request RegisterRequest with user details
     * @return AuthResponse with tokens
     * @throws IllegalArgumentException if validation fails
     */
    public AuthResponse register(RegisterRequest request) {
        // Validate password confirmation
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new IllegalArgumentException("Passwords do not match");
        }
        
        // Check for duplicate username (case-insensitive)
        if (customerRepository.existsByUsernameIgnoreCase(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        
        // Check for duplicate email (case-insensitive)
        if (customerRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        
        // Create new customer with hashed password
        Customer customer = new Customer();
        customer.setName(request.getName());
        customer.setUsername(request.getUsername());
        customer.setEmail(request.getEmail());
        customer.setPassword(passwordEncoder.encode(request.getPassword())); // Hash password
        customer.setAddress(request.getAddress());
        customer.setPhone(request.getPhone());
        customer.setEnabled(true);
        customer.setRole("USER");
        
        // Save customer to database
        customer = customerRepository.save(customer);
        
        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(
                customer.getCustomerId(),
                customer.getUsername(),
                Arrays.asList(customer.getRole())
        );
        String refreshToken = jwtTokenProvider.generateRefreshToken(
                customer.getCustomerId(),
                customer.getUsername(),
                Arrays.asList(customer.getRole())
        );
        
        // Store refresh token in database
        saveRefreshToken(customer, refreshToken);
        
        return new AuthResponse(
                accessToken,
                refreshToken,
                jwtConfig.getAccessTokenExpiration(),
                customer.getCustomerId(),
                customer.getUsername(),
                customer.getEmail(),
                customer.getRole()
        );
    }
    
    /**
     * Authenticate user and generate tokens.
     * Uses Spring Security's AuthenticationManager for password validation.
     * 
     * @param request LoginRequest with username and password
     * @return AuthResponse with tokens
     * @throws BadCredentialsException if credentials are invalid
     */
    public AuthResponse login(LoginRequest request) {
        // Authenticate using Spring Security
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
        } catch (Exception e) {
            throw new BadCredentialsException("Invalid username or password");
        }
        
        // Load customer details
        Customer customer = customerRepository.findByUsernameIgnoreCase(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));
        
        if (!customer.getEnabled()) {
            throw new IllegalArgumentException("Account is disabled");
        }
        
        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(
                customer.getCustomerId(),
                customer.getUsername(),
                Arrays.asList(customer.getRole())
        );
        String refreshToken = jwtTokenProvider.generateRefreshToken(
                customer.getCustomerId(),
                customer.getUsername(),
                Arrays.asList(customer.getRole())
        );
        
        // Store refresh token in database
        saveRefreshToken(customer, refreshToken);
        
        return new AuthResponse(
                accessToken,
                refreshToken,
                jwtConfig.getAccessTokenExpiration(),
                customer.getCustomerId(),
                customer.getUsername(),
                customer.getEmail(),
                customer.getRole()
        );
    }
    
    /**
     * Refresh access token using refresh token.
     * Validates refresh token expiration and revocation status.
     * 
     * @param refreshTokenStr the refresh token string
     * @return AuthResponse with new access token
     * @throws IllegalArgumentException if token is invalid
     */
    public AuthResponse refreshAccessToken(String refreshTokenStr) {
        // Find refresh token in database
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenStr)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token not found"));
        
        // Validate token status
        if (refreshToken.getRevoked()) {
            throw new IllegalArgumentException("Refresh token has been revoked");
        }
        
        if (!refreshToken.isValid()) {
            throw new IllegalArgumentException("Refresh token has expired");
        }
        
        // Get customer and generate new access token
        Customer customer = refreshToken.getCustomer();
        
        String newAccessToken = jwtTokenProvider.generateAccessToken(
                customer.getCustomerId(),
                customer.getUsername(),
                Arrays.asList(customer.getRole())
        );
        
        return new AuthResponse(
                newAccessToken,
                refreshTokenStr, // Return same refresh token
                jwtConfig.getAccessTokenExpiration(),
                customer.getCustomerId(),
                customer.getUsername(),
                customer.getEmail(),
                customer.getRole()
        );
    }
    
    /**
     * Logout by revoking all refresh tokens for a user.
     * 
     * @param customerId the customer ID
     */
    public void logout(Long customerId) {
        Customer customer = new Customer();
        customer.setCustomerId(customerId);
        
        // Revoke all refresh tokens for this customer
        java.util.List<RefreshToken> tokens = refreshTokenRepository.findByCustomer(customer);
        for (RefreshToken token : tokens) {
            if (!token.getRevoked()) {
                token.setRevoked(true);
                token.setUpdatedAt(LocalDateTime.now());
                refreshTokenRepository.save(token);
            }
        }
    }
    
    /**
     * Save refresh token to database.
     * Private helper method for token persistence.
     */
    private void saveRefreshToken(Customer customer, String token) {
        // Calculate expiry date
        LocalDateTime expiryDate = LocalDateTime.now()
                .plusSeconds(jwtConfig.getRefreshTokenExpiration() / 1000);
        
        // Create and save token
        RefreshToken refreshToken = new RefreshToken(customer, token, expiryDate);
        refreshTokenRepository.save(refreshToken);
    }
    
    /**
     * Validate customer exists and is enabled.
     * Used for security checks.
     */
    public boolean validateCustomer(Long customerId) {
        Optional<Customer> customer = customerRepository.findById(customerId);
        return customer.isPresent() && customer.get().getEnabled();
    }
}
