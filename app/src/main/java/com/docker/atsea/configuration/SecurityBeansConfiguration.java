package com.docker.atsea.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Standalone configuration for security beans.
 * Extracted to break circular dependency between AuthService and SecurityConfiguration.
 */
@Configuration
public class SecurityBeansConfiguration {
    
    /**
     * Password encoder using BCrypt for secure password storage.
     * Strength 12 provides good security vs performance tradeoff.
     * 
     * @return BCryptPasswordEncoder with strength 12
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
