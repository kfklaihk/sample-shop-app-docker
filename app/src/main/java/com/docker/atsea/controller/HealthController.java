package com.docker.atsea.controller;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Health check endpoint for core dependencies.
 * Reports status for database, Redis, RabbitMQ, and optional payment gateway.
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);
    private static final Duration HTTP_TIMEOUT = Duration.ofSeconds(3);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Autowired
    private ConnectionFactory rabbitConnectionFactory;

    @Value("${payment.gateway.health-url:}")
    private String paymentGatewayHealthUrl;

    @Value("${payment.gateway.url:}")
    private String paymentGatewayUrl;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(HTTP_TIMEOUT)
            .build();

    @RequestMapping(value = { "", "/" }, method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> checks = new LinkedHashMap<>();
        boolean degraded = false;

        degraded |= !checkDatabase(checks);
        degraded |= !checkRedis(checks);
        degraded |= !checkRabbitMq(checks);
        degraded |= !checkPaymentGateway(checks);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", degraded ? "degraded" : "ok");
        response.put("timestamp", Instant.now().toString());
        response.put("checks", checks);

        return new ResponseEntity<>(response, degraded ? HttpStatus.SERVICE_UNAVAILABLE : HttpStatus.OK);
    }

    private boolean checkDatabase(Map<String, Object> checks) {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            checks.put("database", status("up", null));
            return true;
        } catch (Exception e) {
            checks.put("database", status("down", e.getMessage()));
            logger.warn("Database health check failed.", e);
            return false;
        }
    }

    private boolean checkRedis(Map<String, Object> checks) {
        try (RedisConnection connection = redisConnectionFactory.getConnection()) {
            String pong = connection.ping();
            checks.put("redis", status("up", pong));
            return true;
        } catch (Exception e) {
            checks.put("redis", status("down", e.getMessage()));
            logger.warn("Redis health check failed.", e);
            return false;
        }
    }

    private boolean checkRabbitMq(Map<String, Object> checks) {
        try (Connection connection = rabbitConnectionFactory.createConnection()) {
            checks.put("rabbitmq", status("up", null));
            return true;
        } catch (Exception e) {
            checks.put("rabbitmq", status("down", e.getMessage()));
            logger.warn("RabbitMQ health check failed.", e);
            return false;
        }
    }

    private boolean checkPaymentGateway(Map<String, Object> checks) {
        String url = paymentGatewayHealthUrl;
        if (url == null || url.isBlank()) {
            url = paymentGatewayUrl;
        }
        if (url == null || url.isBlank()) {
            checks.put("paymentGateway", status("skipped", "not configured"));
            return true;
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(HTTP_TIMEOUT)
                    .GET()
                    .build();
            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            if (response.statusCode() >= 200 && response.statusCode() < 400) {
                checks.put("paymentGateway", status("up", "HTTP " + response.statusCode()));
                return true;
            }
            checks.put("paymentGateway", status("down", "HTTP " + response.statusCode()));
            return false;
        } catch (Exception e) {
            checks.put("paymentGateway", status("down", e.getMessage()));
            logger.warn("Payment gateway health check failed.", e);
            return false;
        }
    }

    private Map<String, Object> status(String status, String detail) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", status);
        if (detail != null && !detail.isBlank()) {
            result.put("detail", detail);
        }
        return result;
    }
}
