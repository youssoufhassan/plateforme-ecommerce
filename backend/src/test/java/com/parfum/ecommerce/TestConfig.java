package com.parfum.ecommerce;

import com.parfum.ecommerce.payment.StripeService;
import com.stripe.model.checkout.Session;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.UUID;

@TestConfiguration
public class TestConfig {

    /** Remplace Stripe : renvoie une session factice sans appel réseau. */
    @Bean
    @Primary
    public StripeService stripeService() {
        return new StripeService() {
            @Override
            public boolean isConfigured() {
                return true;
            }

            @Override
            public Session createCheckoutSession(com.parfum.ecommerce.order.Order order) {
                Session session = new Session();
                session.setId("cs_test_" + UUID.randomUUID().toString().replace("-", ""));
                session.setUrl("https://checkout.stripe.com/test/" + session.getId());
                return session;
            }
        };
    }
}