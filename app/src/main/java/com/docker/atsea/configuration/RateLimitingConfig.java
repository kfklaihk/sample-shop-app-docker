package com.docker.atsea.configuration;

import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate Limiting Configuration for preventing brute force attacks.
 * 
 * Rate Limits:
 * - Login endpoint: 5 requests per minute per IP
 * - Register endpoint: 3 requests per minute per IP
 * - Refresh token: 10 requests per minute per IP
 * 
 * Uses in-memory sliding window counter. For production, use Redis.
 */
@Configuration
@Profile("!test")
public class RateLimitingConfig implements WebMvcConfigurer {
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RateLimitingInterceptor())
                .addPathPatterns("/api/auth/**")
                .addPathPatterns("/api/products/**");
    }
    
    /**
     * Rate limiting interceptor using sliding window algorithm.
     * In-memory storage suitable for single-instance deployments.
     * For distributed systems, use Redis-backed implementation.
     */
    public static class RateLimitingInterceptor implements HandlerInterceptor {
        
        // Store: "clientIp:endpoint" -> List of request timestamps
        private final ConcurrentHashMap<String, java.util.List<Long>> requestTimestamps = 
                new ConcurrentHashMap<>();
        
        // Rate limit configurations (requests per minute)
        private static final int LOGIN_RATE_LIMIT = 5;
        private static final int REGISTER_RATE_LIMIT = 3;
        private static final int REFRESH_RATE_LIMIT = 10;
        private static final int GENERAL_RATE_LIMIT = 100;
        private static final long WINDOW_MS = 60000; // 1 minute
        
        @Override
        public boolean preHandle(HttpServletRequest request, 
                                HttpServletResponse response, 
                                Object handler) throws Exception {
            String clientIp = getClientIp(request);
            String path = request.getRequestURI();
            
            // Determine rate limit based on endpoint
            int limit = GENERAL_RATE_LIMIT;
            if (path.contains("/auth/login")) {
                limit = LOGIN_RATE_LIMIT;
            } else if (path.contains("/auth/register")) {
                limit = REGISTER_RATE_LIMIT;
            } else if (path.contains("/auth/refresh-token")) {
                limit = REFRESH_RATE_LIMIT;
            }
            
            String key = clientIp + ":" + path;
            long now = System.currentTimeMillis();
            
            // Get or create request list for this client+endpoint
            java.util.List<Long> timestamps = requestTimestamps.computeIfAbsent(key, 
                    k -> new java.util.concurrent.CopyOnWriteArrayList<>());
            
            // Remove old timestamps outside the window
            timestamps.removeIf(t -> now - t > WINDOW_MS);
            
            // Check if over limit
            if (timestamps.size() >= limit) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.getWriter().write("{\"error\": \"Too many requests. Please try again later.\"}");
                return false;
            }
            
            // Add current request timestamp
            timestamps.add(now);
            
            // Set rate limit headers
            response.setHeader("X-RateLimit-Limit", String.valueOf(limit));
            response.setHeader("X-RateLimit-Remaining", String.valueOf(limit - timestamps.size()));
            response.setHeader("X-RateLimit-Reset", String.valueOf((now + WINDOW_MS) / 1000));
            
            return true;
        }
        
        /**
         * Extract client IP from request, handling proxies and load balancers.
         */
        private String getClientIp(HttpServletRequest request) {
            // Check for X-Forwarded-For header (behind proxy)
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }
            
            // Check for X-Real-IP header
            String xRealIp = request.getHeader("X-Real-IP");
            if (xRealIp != null && !xRealIp.isEmpty()) {
                return xRealIp;
            }
            
            // Fall back to remote address
            return request.getRemoteAddr();
        }
    }
}
