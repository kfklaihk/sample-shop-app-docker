package com.docker.atsea.configuration;

import com.stripe.Stripe;
import com.stripe.net.RequestOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Stripe payment gateway configuration.
 * Initializes Stripe API key for payment processing and webhook handling.
 * 
 * Stripe Integration:
 * - Payment intent creation for orders
 * - Webhook handling for payment status updates
 * - Customer management in Stripe
 * - Refund processing
 * 
 * Configuration requires:
 * - stripe.api-key: Stripe Secret API Key (must start with 'sk_')
 * - stripe.webhook-secret: Webhook signing secret for validating webhook requests
 */
@Configuration
public class StripeConfig {

    @Value("${stripe.api-key}")
    private String stripeApiKey;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    /**
     * Initialize Stripe API key on application startup.
     * This bean is instantiated early in the context lifecycle.
     * 
     * Important: The stripeApiKey must be a valid Stripe Secret Key (sk_live_* or sk_test_*)
     * 
     * @return RequestOptions with configured API key
     */
    @Bean
    public RequestOptions requestOptions() {
        Stripe.apiKey = stripeApiKey;
        return RequestOptions.builder()
                .setApiKey(stripeApiKey)
                .build();
    }

    /**
     * Provides access to the webhook secret for signature verification.
     * Webhook signature verification prevents unauthorized/spoofed webhook calls.
     * 
     * @return the webhook signing secret
     */
    public String getWebhookSecret() {
        return webhookSecret;
    }

    /**
     * Provides access to the Stripe API key.
     * Can be used for additional Stripe operations outside of standard library methods.
     * 
     * @return the Stripe API key
     */
    public String getStripeApiKey() {
        return stripeApiKey;
    }
}
