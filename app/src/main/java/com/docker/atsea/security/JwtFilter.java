package com.docker.atsea.security;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;

/**
 * Legacy JWT filter - Deprecated in favor of JwtAuthenticationFilter.
 * Kept for backward compatibility.
 * 
 * This filter extracts JWT claims and stores them in the request attribute.
 * New implementations should use JwtAuthenticationFilter which integrates
 * with Spring Security's SecurityContext.
 */
@Component
@Deprecated
public class JwtFilter extends GenericFilterBean {
    
    @Autowired(required = false)
    private JwtTokenProvider jwtTokenProvider;

    @Override
    public void doFilter(final ServletRequest req,
                         final ServletResponse res,
                         final FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) req;

        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // Continue without JWT - not all endpoints require authentication
            chain.doFilter(req, res);
            return;
        }

        final String token = authHeader.substring(7); // The part after "Bearer "

        try {
            if (jwtTokenProvider != null && jwtTokenProvider.validateToken(token)) {
                final Claims claims = jwtTokenProvider.extractClaims(token);
                request.setAttribute("claims", claims);
            } else {
                throw new ServletException("Invalid token.");
            }
        }
        catch (final Exception e) {
            throw new ServletException("Invalid token: " + e.getMessage());
        }

        chain.doFilter(req, res);
    }

}
