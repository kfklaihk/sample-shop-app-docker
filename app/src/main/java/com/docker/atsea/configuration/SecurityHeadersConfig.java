package com.docker.atsea.configuration;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Security Headers Configuration for XSS and other protections.
 * 
 * Headers added:
 * - X-Content-Type-Options: nosniff (prevent MIME type sniffing)
 * - X-Frame-Options: DENY (prevent clickjacking)
 * - X-XSS-Protection: 1; mode=block (legacy XSS protection)
 * - Content-Security-Policy: strict-origin-when-cross-origin (XSS prevention)
 * - Strict-Transport-Security: max-age=31536000; includeSubDomains (HTTPS enforcement)
 * - Referrer-Policy: strict-origin-when-cross-origin (privacy)
 */
@Configuration
public class SecurityHeadersConfig {
    
    @Bean
    public FilterRegistrationBean<SecurityHeadersFilter> securityHeadersFilter() {
        FilterRegistrationBean<SecurityHeadersFilter> registrationBean = 
                new FilterRegistrationBean<>(new SecurityHeadersFilter());
        registrationBean.setOrder(Integer.MIN_VALUE); // Execute first
        return registrationBean;
    }
    
    /**
     * Filter that adds security headers to all responses.
     */
    public static class SecurityHeadersFilter extends OncePerRequestFilter {
        
        @Override
        protected void doFilterInternal(HttpServletRequest request, 
                                       HttpServletResponse response, 
                                       FilterChain filterChain)
                throws ServletException, IOException {
            
            // Prevent MIME type sniffing (XSS protection)
            response.setHeader("X-Content-Type-Options", "nosniff");
            
            // Prevent clickjacking attacks
            response.setHeader("X-Frame-Options", "DENY");
            
            // Legacy XSS protection header (modern browsers use CSP)
            response.setHeader("X-XSS-Protection", "1; mode=block");
            
            // Content Security Policy - strict protection against XSS
            // Only allow scripts from same origin
            response.setHeader("Content-Security-Policy", 
                    "default-src 'self'; " +
                    "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
                    "style-src 'self' 'unsafe-inline'; " +
                    "img-src 'self' data: https:; " +
                    "font-src 'self'; " +
                    "connect-src 'self'; " +
                    "frame-ancestors 'none'; " +
                    "base-uri 'self'; " +
                    "form-action 'self'");
            
            // Enforce HTTPS (only in production)
            String scheme = request.getScheme();
            if ("https".equals(scheme)) {
                response.setHeader("Strict-Transport-Security", 
                        "max-age=31536000; includeSubDomains; preload");
            }
            
            // Referrer policy for privacy
            response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
            
            // Permissions policy (formerly Feature-Policy)
            response.setHeader("Permissions-Policy", 
                    "geolocation=(), microphone=(), camera=(), payment=()");
            
            filterChain.doFilter(request, response);
        }
    }
}
