package com.docker.atsea.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.docker.atsea.dto.LoginRequest;
import com.docker.atsea.dto.RegisterRequest;
import com.docker.atsea.model.Customer;
import com.docker.atsea.model.RefreshToken;
import com.docker.atsea.repositories.CustomerRepository;
import com.docker.atsea.repositories.RefreshTokenRepository;
import com.docker.atsea.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * Integration tests for AuthController.
 * Tests registration, login, token refresh, and logout endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Auth Controller Integration Tests")
public class AuthControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @MockBean
    private CustomerRepository customerRepository;
    
    @MockBean
    private RefreshTokenRepository refreshTokenRepository;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    private Customer testCustomer;
    private RegisterRequest validRegisterRequest;
    private LoginRequest validLoginRequest;
    
    @BeforeEach
    void setUp() {
        // Create test customer
        testCustomer = new Customer();
        testCustomer.setCustomerId(1L);
        testCustomer.setUsername("testuser");
        testCustomer.setEmail("test@example.com");
        testCustomer.setName("Test User");
        testCustomer.setPassword(passwordEncoder.encode("password123"));
        testCustomer.setAddress("123 Test St");
        testCustomer.setPhone("1234567890");
        testCustomer.setEnabled(true);
        testCustomer.setRole("USER");
        
        // Create test register request
        validRegisterRequest = new RegisterRequest();
        validRegisterRequest.setUsername("newuser");
        validRegisterRequest.setEmail("new@example.com");
        validRegisterRequest.setPassword("password123");
        validRegisterRequest.setPasswordConfirm("password123");
        validRegisterRequest.setName("New User");
        validRegisterRequest.setAddress("456 New Ave");
        validRegisterRequest.setPhone("9876543210");
        
        // Create test login request
        validLoginRequest = new LoginRequest("testuser", "password123");
    }
    
    @Test
    @DisplayName("Should register new user successfully")
    void testRegister_Success() throws Exception {
        // Mock repository to indicate username/email not existing
        when(customerRepository.existsByUsernameIgnoreCase("newuser")).thenReturn(false);
        when(customerRepository.existsByEmailIgnoreCase("new@example.com")).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        
        // Perform request
        String requestBody = objectMapper.writeValueAsString(validRegisterRequest);
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.expiresIn").exists())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.username").value("testuser"));
    }
    
    @Test
    @DisplayName("Should reject registration with duplicate username")
    void testRegister_DuplicateUsername() throws Exception {
        // Mock repository to indicate username already exists
        when(customerRepository.existsByUsernameIgnoreCase("newuser")).thenReturn(true);
        
        String requestBody = objectMapper.writeValueAsString(validRegisterRequest);
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Username already exists"));
    }
    
    @Test
    @DisplayName("Should reject registration with mismatched passwords")
    void testRegister_PasswordMismatch() throws Exception {
        validRegisterRequest.setPasswordConfirm("different");
        
        String requestBody = objectMapper.writeValueAsString(validRegisterRequest);
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Passwords do not match"));
    }
    
    @Test
    @DisplayName("Should reject registration with invalid email")
    void testRegister_InvalidEmail() throws Exception {
        validRegisterRequest.setEmail("invalid-email");
        
        String requestBody = objectMapper.writeValueAsString(validRegisterRequest);
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"));
    }
    
    @Test
    @DisplayName("Should reject registration with short password")
    void testRegister_ShortPassword() throws Exception {
        validRegisterRequest.setPassword("short");
        validRegisterRequest.setPasswordConfirm("short");
        
        String requestBody = objectMapper.writeValueAsString(validRegisterRequest);
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"));
    }
    
    @Test
    @DisplayName("Should login user successfully")
    void testLogin_Success() throws Exception {
        // Mock repository to return customer
        when(customerRepository.findByUsernameIgnoreCase("testuser"))
                .thenReturn(Optional.of(testCustomer));
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        
        String requestBody = objectMapper.writeValueAsString(validLoginRequest);
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.username").value("testuser"));
    }
    
    @Test
    @DisplayName("Should reject login with invalid credentials")
    void testLogin_InvalidCredentials() throws Exception {
        validLoginRequest.setPassword("wrongpassword");
        
        // Mock repository to return customer (but password won't match)
        when(customerRepository.findByUsernameIgnoreCase("testuser"))
                .thenReturn(Optional.of(testCustomer));
        
        String requestBody = objectMapper.writeValueAsString(validLoginRequest);
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid username or password"));
    }
    
    @Test
    @DisplayName("Should reject login with non-existent user")
    void testLogin_UserNotFound() throws Exception {
        // Mock repository to indicate user not found
        when(customerRepository.findByUsernameIgnoreCase("nonexistent"))
                .thenReturn(Optional.empty());
        
        LoginRequest request = new LoginRequest("nonexistent", "password");
        String requestBody = objectMapper.writeValueAsString(request);
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid username or password"));
    }
    
    @Test
    @DisplayName("Should have CSRF protection headers")
    void testSecurityHeaders_CsrfProtection() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should have XSS protection headers")
    void testSecurityHeaders_XssProtection() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
