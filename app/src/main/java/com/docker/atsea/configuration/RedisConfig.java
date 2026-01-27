package com.docker.atsea.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;


/**
 * Redis configuration for distributed caching and session management.
 * Enables Spring's caching abstraction with Redis backend.
 * 
 * Cache Configuration:
 * - Product catalog: 1-hour TTL
 * - Customer sessions: 24-hour TTL
 * - Order status: Real-time updates
 */
@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * Lettuce connection factory for Redis.
     * Lettuce is a non-blocking, thread-safe Redis client library.
     * 
     * @return RedisConnectionFactory configured for Lettuce
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory(
            @Value("${spring.redis.host:localhost}") String host,
            @Value("${spring.redis.port:6379}") int port) {
        return new LettuceConnectionFactory(host, port);
    }

    /**
     * Redis template for low-level operations.
     * Configures serialization strategy for keys and values.
     * 
     * @param connectionFactory the Redis connection factory
     * @return RedisTemplate with JSON serialization
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // String key serialization
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        
        // JSON value serialization
        GenericJackson2JsonRedisSerializer jsonRedisSerializer = new GenericJackson2JsonRedisSerializer();

        // Configure key-value serialization
        template.setKeySerializer(stringRedisSerializer);
        template.setValueSerializer(jsonRedisSerializer);
        
        // Configure hash key-value serialization
        template.setHashKeySerializer(stringRedisSerializer);
        template.setHashValueSerializer(jsonRedisSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * Cache manager for Spring's @Cacheable annotations.
     * Uses Redis as the backend cache store.
     * 
     * @param connectionFactory the Redis connection factory
     * @return RedisCacheManager for managing caches
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        return RedisCacheManager.create(connectionFactory);
    }
}
