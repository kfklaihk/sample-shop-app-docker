package com.docker.atsea.security;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * JWT Authentication Filter for validating JWT tokens in request headers.
 * Extracts JWT from Authorization header, validates it, and populates SecurityContext.
 * 
 * Expected Header Format: Authorization: Bearer <jwt-token>
 * 
 * This filter is executed once per request and sets up the SecurityContext
 * with user authentication before the request reaches controllers.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    /**
     * Filter method that validates JWT tokens in incoming requests.
     * 
     * Process:
     * 1. Extract JWT token from Authorization header
     * 2. Validate token signature and expiration
     * 3. Extract claims (userId, username, roles)
     * 4. Create Authentication object and set in SecurityContext
     * 
     * @param request the HTTP request
     * @param response the HTTP response
     * @param filterChain the filter chain
     * @throws ServletException if filter processing fails
     * @throws IOException if I/O error occurs
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain)
            throws ServletException, IOException {
        
        try {
            String token = extractTokenFromRequest(request);
            
            if (token != null && jwtTokenProvider.validateToken(token)) {
                // Token is valid - extract user info and set in SecurityContext
                String username = jwtTokenProvider.getUsernameFromToken(token);
                Long userId = jwtTokenProvider.getUserIdFromToken(token);
                List<String> roles = jwtTokenProvider.getRolesFromToken(token);
                
                // Convert roles to Spring Security authorities
                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .toList();
                
                // Create authentication token
                UsernamePasswordAuthenticationToken authToken = 
                        new UsernamePasswordAuthenticationToken(username, null, authorities);
                
                // Store user ID as principal detail for later access
                authToken.setDetails(userId);
                
                // Set authentication in SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (Exception e) {
            // Log token validation error but continue filter chain
            // Unauthorized access will be handled by @PreAuthorize annotations
            logger.debug("JWT validation failed: " + e.getMessage());
        }
        
        // Continue filter chain
        filterChain.doFilter(request, response);
    }
    
    /**
     * Extract JWT token from Authorization header.
     * Expected format: "Bearer <token>"
     * 
     * @param request the HTTP request
     * @return JWT token string, or null if not found or invalid format
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7); // Remove "Bearer " prefix
        }
        
        return null;
    }
}
