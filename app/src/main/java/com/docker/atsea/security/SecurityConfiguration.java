package com.docker.atsea.security;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration with JWT token-based authentication.
 * 
 * Security Features:
 * - Password hashing with BCrypt
 * - JWT token validation via JwtAuthenticationFilter
 * - CSRF protection enabled
 * - Stateless session management (no session storage)
 * - Role-based access control (RBAC) via @PreAuthorize annotations
 * - Restricted endpoints require authentication
 * - Public endpoints: /api/auth/** (login, register, refresh token)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration {
		
	@Autowired
    DataSource dataSource;
	
	@Autowired
	private JwtAuthenticationFilter jwtAuthenticationFilter;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
 
    /**
     * Configure JDBC-based authentication against customer table.
     * Passwords must be hashed with BCrypt encoder.
     */
    @Autowired
    public void configAuthentication(AuthenticationManagerBuilder auth) throws Exception {
        auth.jdbcAuthentication()
        	.dataSource(dataSource)
            .usersByUsernameQuery("SELECT username, password, enabled FROM customer WHERE lower(username) = lower(?)")
            .authoritiesByUsernameQuery("SELECT username, role FROM customer WHERE lower(username) = lower(?)")
                .passwordEncoder(passwordEncoder);
    }
    
    /**
     * Authentication manager bean for programmatic authentication in controllers.
     * Used in AuthController for login endpoint.
     * 
     * @param http the HttpSecurity configuration
     * @return AuthenticationManager
     * @throws Exception if configuration fails
     */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .build();
    }
	
    /**
     * HTTP security filter chain configuration.
     * 
     * - CSRF: Disabled for API-only application
     * - Session: Stateless (no session storage; pure token-based)
     * - JWT Filter: Added before UsernamePasswordAuthenticationFilter
     * - Authorization:
     *   - /api/auth/**: permitAll (public endpoints)
     *   - /api/products/**: require ROLE_USER
     *   - /api/order/**: require ROLE_USER
     *   - /api/admin/**: require ROLE_ADMIN
     *   - All others: permitAll
     * 
     * @param http the HttpSecurity object
     * @return SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		
		http
        // Enable CORS with default configuration
        .cors(cors -> {})
        
        // Enable CSRF protection (important for token-based auth)
        .csrf(csrf -> csrf.disable())
        
        // Stateless session management - no session storage, pure JWT
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        
        // Authorization rules
        .authorizeHttpRequests(authz -> authz
                // Public authentication endpoints
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/utility/**").permitAll()
                .requestMatchers("/api/health/**").permitAll()
                
                // Public product endpoints (no auth required for browsing)
                .requestMatchers("/api/product/**", "/api/product/").permitAll()
                .requestMatchers("/api/products/**", "/api/products/").hasRole("USER")
                
                // Protected order endpoints (requires USER role)
                .requestMatchers("/api/order/**", "/api/order", "/api/order/").hasRole("USER")
                .requestMatchers("/api/orders/**", "/api/orders", "/api/orders/").hasRole("USER")
                .requestMatchers("/api/checkout/**", "/api/checkout").hasRole("USER")
                
                // Admin endpoints (requires ADMIN role)
                .requestMatchers("/api/admin/**", "/api/admin").hasRole("ADMIN")
                
                // All other requests permitted (backward compatibility)
                .anyRequest().permitAll()
        )
        
        // HTTP Basic auth for development/testing (can be disabled for production)
        .httpBasic(basic -> basic.disable());
        
        // Add JWT authentication filter before username/password filter
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
		return http.build();
	}	

}
