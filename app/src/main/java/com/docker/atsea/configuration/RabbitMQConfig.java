package com.docker.atsea.configuration;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for asynchronous order processing and event handling.
 * Defines message queues, exchanges, and bindings for handling order events.
 * 
 * Queue Architecture:
 * - orders.created: New orders published here
 * - orders.processing: Orders being processed
 * - orders.completed: Completed orders
 * - orders.failed: Failed orders (DLQ)
 * 
 * Exchange: orders.exchange (Direct Exchange)
 * Routing Keys: order.created, order.processing, order.completed, order.failed
 */
@Configuration
@EnableRabbit
public class RabbitMQConfig {

    // Queue names
    public static final String ORDERS_QUEUE = "orders.created";
    public static final String ORDERS_PROCESSING_QUEUE = "orders.processing";
    public static final String ORDERS_COMPLETED_QUEUE = "orders.completed";
    public static final String ORDERS_FAILED_QUEUE = "orders.failed";

    // Exchange and routing key names
    public static final String ORDERS_EXCHANGE = "orders.exchange";
    public static final String ORDERS_ROUTING_KEY = "order.created";
    public static final String ORDERS_PROCESSING_ROUTING_KEY = "order.processing";
    public static final String ORDERS_COMPLETED_ROUTING_KEY = "order.completed";
    public static final String ORDERS_FAILED_ROUTING_KEY = "order.failed";

    /**
     * Queue for newly created orders.
     * TTL: 24 hours
     * Max retries: 3 (handled by consumer)
     * 
     * @return Queue for order creation events
     */
    @Bean
    public Queue ordersQueue() {
        return QueueBuilder.durable(ORDERS_QUEUE)
                .ttl(86400000) // 24 hours in milliseconds
                .build();
    }

    /**
     * Queue for orders being processed.
     * 
     * @return Queue for order processing events
     */
    @Bean
    public Queue ordersProcessingQueue() {
        return QueueBuilder.durable(ORDERS_PROCESSING_QUEUE)
                .ttl(86400000)
                .build();
    }

    /**
     * Queue for completed orders.
     * Archived for audit trail and analytics.
     * 
     * @return Queue for order completion events
     */
    @Bean
    public Queue ordersCompletedQueue() {
        return QueueBuilder.durable(ORDERS_COMPLETED_QUEUE)
                .ttl(604800000) // 7 days in milliseconds
                .build();
    }

    /**
     * Dead Letter Queue (DLQ) for failed orders.
     * Orders that fail payment or processing retry logic move here.
     * 
     * @return Queue for failed orders
     */
    @Bean
    public Queue ordersFailedQueue() {
        return QueueBuilder.durable(ORDERS_FAILED_QUEUE)
                .ttl(604800000) // 7 days in milliseconds
                .build();
    }

    /**
     * Direct exchange for routing order events.
     * Direct exchanges route messages to queues based on exact routing key match.
     * 
     * @return DirectExchange for orders
     */
    @Bean
    public DirectExchange ordersExchange() {
        return new DirectExchange(ORDERS_EXCHANGE, true, false);
    }

    /**
     * Binding between orders.created queue and orders.exchange with routing key.
     * 
     * @param ordersQueue the orders queue
     * @param ordersExchange the orders exchange
     * @return Binding configuration
     */
    @Bean
    public Binding ordersBinding(Queue ordersQueue, DirectExchange ordersExchange) {
        return BindingBuilder.bind(ordersQueue)
                .to(ordersExchange)
                .with(ORDERS_ROUTING_KEY);
    }

    /**
     * Binding for orders processing queue.
     * 
     * @param ordersProcessingQueue the orders processing queue
     * @param ordersExchange the orders exchange
     * @return Binding configuration
     */
    @Bean
    public Binding ordersProcessingBinding(Queue ordersProcessingQueue, DirectExchange ordersExchange) {
        return BindingBuilder.bind(ordersProcessingQueue)
                .to(ordersExchange)
                .with(ORDERS_PROCESSING_ROUTING_KEY);
    }

    /**
     * Binding for orders completed queue.
     * 
     * @param ordersCompletedQueue the orders completed queue
     * @param ordersExchange the orders exchange
     * @return Binding configuration
     */
    @Bean
    public Binding ordersCompletedBinding(Queue ordersCompletedQueue, DirectExchange ordersExchange) {
        return BindingBuilder.bind(ordersCompletedQueue)
                .to(ordersExchange)
                .with(ORDERS_COMPLETED_ROUTING_KEY);
    }

    /**
     * Binding for orders failed queue (DLQ).
     * 
     * @param ordersFailedQueue the orders failed queue
     * @param ordersExchange the orders exchange
     * @return Binding configuration
     */
    @Bean
    public Binding ordersFailedBinding(Queue ordersFailedQueue, DirectExchange ordersExchange) {
        return BindingBuilder.bind(ordersFailedQueue)
                .to(ordersExchange)
                .with(ORDERS_FAILED_ROUTING_KEY);
    }

    /**
     * RabbitTemplate for sending messages.
     * Configured with Jackson JSON message converter.
     * 
     * @param connectionFactory the RabbitMQ connection factory
     * @return RabbitTemplate for message operations
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jackson2JsonMessageConverter());
        return template;
    }

    /**
     * Jackson2 message converter for JSON serialization of messages.
     * Ensures messages are sent/received as JSON objects.
     * 
     * @return MessageConverter for JSON serialization
     */
    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
