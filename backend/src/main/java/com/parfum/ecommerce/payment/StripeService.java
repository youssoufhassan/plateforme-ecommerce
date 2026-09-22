package com.parfum.ecommerce.payment;

import com.parfum.ecommerce.order.Order;
import com.parfum.ecommerce.order.OrderItem;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class StripeService {

    @Value("${stripe.secret-key}")
    private String secretKey;

    @Value("${stripe.success-url}")
    private String successUrl;

    @Value("${stripe.cancel-url}")
    private String cancelUrl;

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }

    public boolean isConfigured() {
        return secretKey != null && !secretKey.isBlank();
    }

    public Session createCheckoutSession(Order order) throws StripeException {
        SessionCreateParams.Builder builder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl + "?order=" + order.getId())
                .setCancelUrl(cancelUrl)
                .setClientReferenceId(order.getId().toString())
                .putMetadata("orderId", order.getId().toString());

        if (order.getUser() != null && order.getUser().getEmail() != null) {
            builder.setCustomerEmail(order.getUser().getEmail());
        }

        for (OrderItem item : order.getItems()) {
            long unitAmountCents = item.getUnitPrice()
                    .multiply(BigDecimal.valueOf(100))
                    .longValueExact();

            builder.addLineItem(
                SessionCreateParams.LineItem.builder()
                    .setQuantity((long) item.getQuantity())
                    .setPriceData(
                        SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency("eur")
                            .setUnitAmount(unitAmountCents)
                            .setProductData(
                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                    .setName(item.getDisplayName())
                                    .build())
                            .build())
                    .build()
            );
        }

        // Frais de livraison en ligne séparée
        if (order.getShippingAmount() != null
                && order.getShippingAmount().compareTo(BigDecimal.ZERO) > 0) {

            long shippingCents = order.getShippingAmount()
                    .multiply(BigDecimal.valueOf(100))
                    .longValueExact();

            builder.addLineItem(
                SessionCreateParams.LineItem.builder()
                    .setQuantity(1L)
                    .setPriceData(
                        SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency("eur")
                            .setUnitAmount(shippingCents)
                            .setProductData(
                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                    .setName("Frais de livraison")
                                    .build())
                            .build())
                    .build()
            );
        }

        return Session.create(builder.build());
    }
}