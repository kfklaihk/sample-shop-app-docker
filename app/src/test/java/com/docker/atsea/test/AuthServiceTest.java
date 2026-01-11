package com.docker.atsea.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

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
 * Unit tests for AuthService with pure mocks (no Spring context).
 * Tests core authentication business logic.
 */
@DisplayName("Auth Service Unit Tests")
public class AuthServiceTest {
    
    @Mock
    private CustomerRepository customerRepository;
    
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    
    @Mock
    private AuthenticationManager authenticationManager;
    
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    
    @Mock
    private JwtConfig jwtConfig;
    
    @InjectMocks
    private AuthService authService;
    
    private PasswordEncoder passwordEncoder;
    private Customer testCustomer;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup password encoder
        passwordEncoder = new BCryptPasswordEncoder(12);
        
        // Create JWT config with test values
        jwtConfig = new JwtConfig();
        jwtConfig.setSecret("test-secret-key-32-characters-long-123456789");
        jwtConfig.setAccessTokenExpiration(900000L);
        jwtConfig.setRefreshTokenExpiration(604800000L);
        jwtConfig.setUserIdClaimKey("userId");
        jwtConfig.setRolesClaimKey("roles");
        
        // Initialize JWT token provider
        jwtTokenProvider = new JwtTokenProvider();
        try {
            var field = JwtTokenProvider.class.getDeclaredField("jwtConfig");
            field.setAccessible(true);
            field.set(jwtTokenProvider, jwtConfig);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Failed to set jwtConfig: " + e.getMessage());
        }
        
        // Manually inject into AuthService
        try {
            var jwtField = AuthService.class.getDeclaredField("jwtTokenProvider");
            jwtField.setAccessible(true);
            jwtField.set(authService, jwtTokenProvider);
            
            var pwdField = AuthService.class.getDeclaredField("passwordEncoder");
            pwdField.setAccessible(true);
            pwdField.set(authService, passwordEncoder);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Failed to inject dependencies: " + e.getMessage());
        }
        
        // Setup test customer
        testCustomer = new Customer();
        testCustomer.setCustomerId(1L);
        testCustomer.setUsername("testuser");
        testCustomer.setEmail("test@example.com");
        testCustomer.setPassword(passwordEncoder.encode("password123"));
        testCustomer.setEnabled(true);
        testCustomer.setRole("USER");
        testCustomer.setName("Test User");
    }
    
    @Test
    @DisplayName("Should successfully register user with valid data")
    void testRegister_Success() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("new@example.com");
        request.setPassword("password123");
        request.setPasswordConfirm("password123");
        request.setName("New User");
        
        when(customerRepository.existsByUsernameIgnoreCase("newuser")).thenReturn(false);
        when(customerRepository.existsByEmailIgnoreCase("new@example.com")).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> {
            Customer customer = invocation.getArgument(0);
            customer.setCustomerId(2L);
            return customer;
        });
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        AuthResponse response = authService.register(request);
        
        // Assert
        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertEquals("newuser", response.getUsername());
        verify(customerRepository, times(1)).save(any(Customer.class));
    }
    
    @Test
    @DisplayName("Should reject registration with mismatched passwords")
    void testRegister_PasswordMismatch() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("new@example.com");
        request.setPassword("password123");
        request.setPasswordConfirm("wrongpassword");
        
        assertThrows(IllegalArgumentException.class, () -> authService.register(request));
    }
    
    @Test
    @DisplayName("Should reject registration with duplicate username")
    void testRegister_DuplicateUsername() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existinguser");
        request.setEmail("new@example.com");
        request.setPassword("password123");
        request.setPasswordConfirm("password123");
        
        when(customerRepository.existsByUsernameIgnoreCase("existinguser")).thenReturn(true);
        
        assertThrows(IllegalArgumentException.class, () -> authService.register(request));
    }
    
    @Test
    @DisplayName("Should reject registration with duplicate email")
    void testRegister_DuplicateEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("existing@example.com");
        request.setPassword("password123");
        request.setPasswordConfirm("password123");
        
        when(customerRepository.existsByUsernameIgnoreCase("newuser")).thenReturn(false);
        when(customerRepository.existsByEmailIgnoreCase("existing@example.com")).thenReturn(true);
        
        assertThrows(IllegalArgumentException.class, () -> authService.register(request));
    }
    
    @Test
    @DisplayName("Should hash password during registration")
    void testRegister_PasswordHashing() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("new@example.com");
        request.setPassword("mysecretpassword");
        request.setPasswordConfirm("mysecretpassword");
        request.setName("New User");
        
        when(customerRepository.existsByUsernameIgnoreCase("newuser")).thenReturn(false);
        when(customerRepository.existsByEmailIgnoreCase("new@example.com")).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> {
            Customer customer = invocation.getArgument(0);
            customer.setCustomerId(2L);
            return customer;
        });
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        
        authService.register(request);
        
        // Verify password was hashed (not plaintext)
        verify(customerRepository, times(1)).save(argThat(customer -> 
            !customer.getPassword().equals("mysecretpassword") && 
            passwordEncoder.matches("mysecretpassword", customer.getPassword())
        ));
    }
    
    @Test
    @DisplayName("Should successfully login with valid credentials")
    void testLogin_Success() {
        LoginRequest request = new LoginRequest("testuser", "password123");
        
        when(customerRepository.findByUsernameIgnoreCase("testuser"))
                .thenReturn(Optional.of(testCustomer));
        when(authenticationManager.authenticate(any()))
                .thenReturn(new UsernamePasswordAuthenticationToken("testuser", "password123"));
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        
        AuthResponse response = authService.login(request);
        
        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertEquals("testuser", response.getUsername());
    }
    
    @Test
    @DisplayName("Should reject login with invalid password")
    void testLogin_InvalidPassword() {
        LoginRequest request = new LoginRequest("testuser", "wrongpassword");
        
        when(authenticationManager.authenticate(any()))
                .thenThrow(new org.springframework.security.authentication.BadCredentialsException("Invalid"));
        
        assertThrows(org.springframework.security.authentication.BadCredentialsException.class, 
            () -> authService.login(request));
    }
    
    @Test
    @DisplayName("Should reject login for disabled account")
    void testLogin_DisabledAccount() {
        LoginRequest request = new LoginRequest("testuser", "password123");
        testCustomer.setEnabled(false);
        
        when(authenticationManager.authenticate(any()))
                .thenThrow(new org.springframework.security.authentication.DisabledException("Disabled"));
        
        assertThrows(org.springframework.security.authentication.BadCredentialsException.class, 
            () -> authService.login(request));
    }
    
    @Test
    @DisplayName("Should validate and logout customer")
    void testLogout_Success() {
        when(refreshTokenRepository.findByCustomer(testCustomer))
                .thenReturn(Arrays.asList());
        
        assertDoesNotThrow(() -> authService.logout(1L));
    }
    
    @Test
    @DisplayName("Should validate existing customer")
    void testValidateCustomer_Exists() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        
        boolean result = authService.validateCustomer(1L);
        
        assertTrue(result);
    }
    
    @Test
    @DisplayName("Should fail validation for non-existent customer")
    void testValidateCustomer_NotExists() {
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());
        
        boolean result = authService.validateCustomer(999L);
        
        assertFalse(result);
    }
}
