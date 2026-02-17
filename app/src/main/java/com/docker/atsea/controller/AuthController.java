package com.docker.atsea.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.docker.atsea.dto.AuthResponse;
import com.docker.atsea.dto.LoginRequest;
import com.docker.atsea.dto.RefreshTokenRequest;
import com.docker.atsea.dto.RegisterRequest;
import com.docker.atsea.service.AuthService;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Authentication Controller for user login, registration, and token management.
 * 
 * Security Features:
 * - Input validation (@Valid annotations)
 * - Password encryption (BCrypt via AuthService)
 * - JWT token generation and refresh
 * - Token revocation on logout
 * - Rate limiting (5 req/min for login, 3 req/min for register)
 * - XSS prevention (Content Security Policy headers)
 * - CSRF protection (stateless JWT, no session cookies)
 * - SQL injection prevention (JPA parameterized queries)
 * - Secure password comparison (Spring Security)
 * 
 * Endpoints:
 * - POST /api/auth/register - User registration
 * - POST /api/auth/login - User login
 * - POST /api/auth/refresh-token - Refresh access token
 * - POST /api/auth/logout - Logout (revoke tokens)
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    private AuthService authService;
    
    /**
     * Register new user.
     * 
     * Validates:
     * - Password confirmation match
     * - Unique username (case-insensitive)
     * - Unique email (case-insensitive)
     * - Email format
     * - Password strength (8+ chars)
     * - Field length constraints
     * 
     * Rate limit: 3 requests per minute per IP
     * 
     * @param request the registration request with user details
     * @param bindingResult validation results
     * @return AuthResponse with JWT tokens on success
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request,
                                     BindingResult bindingResult) {
        try {
            // Check for validation errors
            if (bindingResult.hasErrors()) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Validation failed");
                error.put("details", bindingResult.getAllErrors()
                        .stream()
                        .map(e -> e.getDefaultMessage())
                        .collect(Collectors.toList()));
                return ResponseEntity.badRequest().body(error);
            }
            
            // Register user (checks for duplicate username/email)
            AuthResponse response = authService.register(request);
            
            logger.info("User registered successfully: {}", request.getUsername());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Registration validation failed: {}", e.getMessage());
            
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
            
        } catch (Exception e) {
            logger.error("Registration error: {}", e.getMessage(), e);
            
            Map<String, String> error = new HashMap<>();
            error.put("error", "Registration failed. Please try again later.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * User login with username and password.
     * 
     * Process:
     * 1. Validate credentials against database (BCrypt comparison)
     * 2. Generate JWT access token (15 min expiration)
     * 3. Generate JWT refresh token (7 days expiration)
     * 4. Store refresh token in database
     * 5. Return tokens and user info
     * 
     * Rate limit: 5 requests per minute per IP (brute force protection)
     * 
     * @param request the login request with username and password
     * @param bindingResult validation results
     * @return AuthResponse with JWT tokens on success
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request,
                                   BindingResult bindingResult) {
        try {
            // Check for validation errors
            if (bindingResult.hasErrors()) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Validation failed");
                error.put("details", bindingResult.getAllErrors()
                        .stream()
                        .map(e -> e.getDefaultMessage())
                        .collect(Collectors.toList()));
                return ResponseEntity.badRequest().body(error);
            }
            
            // Authenticate user (uses Spring Security's AuthenticationManager)
            AuthResponse response = authService.login(request);
            
            logger.info("User logged in successfully: {}", request.getUsername());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.warn("Login failed for user: {}", request.getUsername());
            
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid username or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }
    
    /**
     * Refresh access token using refresh token.
     * 
     * Process:
     * 1. Validate refresh token exists in database
     * 2. Check token expiration and revocation status
     * 3. Generate new access token
     * 4. Return new token with same refresh token
     * 
     * Rate limit: 10 requests per minute per IP
     * 
     * @param request the refresh token request
     * @param bindingResult validation results
     * @return AuthResponse with new access token
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest request,
                                         BindingResult bindingResult) {
        try {
            // Check for validation errors
            if (bindingResult.hasErrors()) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Validation failed");
                error.put("details", bindingResult.getAllErrors()
                        .stream()
                        .map(e -> e.getDefaultMessage())
                        .collect(Collectors.toList()));
                return ResponseEntity.badRequest().body(error);
            }
            
            // Refresh token (validates against database)
            AuthResponse response = authService.refreshAccessToken(request.getRefreshToken());
            
            logger.debug("Access token refreshed");
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Token refresh failed: {}", e.getMessage());
            
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            
        } catch (Exception e) {
            logger.error("Token refresh error: {}", e.getMessage(), e);
            
            Map<String, String> error = new HashMap<>();
            error.put("error", "Token refresh failed");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Logout by revoking all refresh tokens.
     * 
     * Process:
     * 1. Extract user ID from JWT token in SecurityContext
     * 2. Revoke all refresh tokens for this user
     * 3. Return success response
     * 
     * Frontend should:
     * 1. Call this endpoint
     * 2. Clear accessToken and refreshToken from localStorage
     * 3. Redirect to login page
     * 
     * @return success message
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        try {
            // Get authenticated user from SecurityContext
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Get user ID from authentication details
            Long userId = (Long) authentication.getDetails();
            
            if (userId == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User ID not found");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Revoke all tokens
            authService.logout(userId);
            
            logger.info("User logged out: {}", authentication.getPrincipal());
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Logged out successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Logout error: {}", e.getMessage(), e);
            
            Map<String, String> error = new HashMap<>();
            error.put("error", "Logout failed");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
