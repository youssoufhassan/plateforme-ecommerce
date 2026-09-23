package com.parfum.ecommerce;

import com.parfum.ecommerce.catalog.Product;
import com.parfum.ecommerce.identity.Address;
import com.parfum.ecommerce.identity.User;
import com.parfum.ecommerce.order.Order;
import com.parfum.ecommerce.order.OrderRepository;
import com.parfum.ecommerce.payment.PaymentRepository;
import com.parfum.ecommerce.payment.PaymentService;
import com.parfum.ecommerce.invoice.InvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestConfig.class)
@DisplayName("Parcours d'achat complet")
class OrderFlowIntegrationTest extends AbstractIntegrationTest {

    @Autowired private TestDataHelper data;
    @Autowired private OrderRepository orderRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private InvoiceRepository invoiceRepository;
    @Autowired private PaymentService paymentService;

    /** L'envoi d'emails est neutralisé : on teste le métier, pas la messagerie. */
    @MockitoBean private com.parfum.ecommerce.mail.EmailService emailService;

    private String token;
    private User user;
    private Address address;
    private Product product;
    private UUID variantId;

    @BeforeEach
    void setUp() throws Exception {
        String email = "client-" + UUID.randomUUID() + "@test.local";
        user = data.createVerifiedUser(email, "motdepasse123");
        address = data.createAddress(user);
        product = data.createProduct("Parfum Test " + UUID.randomUUID(), new BigDecimal("30.00"), 5);
        variantId = data.firstVariantId(product);

        String response = mvc.perform(post("/api/auth/login")
                        .contentType(jsonType())
                        .content(json(Map.of("email", email, "password", "motdepasse123"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        token = objectMapper.readTree(response).get("token").asText();
    }

    @Test
    @DisplayName("Le stock n'est décrémenté qu'après confirmation du paiement")
    void stockDecrementedOnlyAfterPayment() throws Exception {
        int stockInitial = data.stockOf(variantId);

        addToCart(1);
        String orderId = checkout();

        assertThat(data.stockOf(variantId))
                .as("Le stock ne doit pas bouger au checkout")
                .isEqualTo(stockInitial);

        pay(orderId);
        confirmPayment(orderId);

        assertThat(data.stockOf(variantId))
                .as("Le stock doit être décrémenté après paiement")
                .isEqualTo(stockInitial - 1);
    }

    @Test
    @DisplayName("Le checkout est refusé sans acceptation des CGV")
    void checkoutRequiresTermsAcceptance() throws Exception {
        addToCart(1);

        mvc.perform(post("/api/orders/checkout")
                        .header("Authorization", "Bearer " + token)
                        .contentType(jsonType())
                        .content(json(Map.of("addressId", address.getId().toString()))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Le checkout est refusé sans adresse de livraison")
    void checkoutRequiresAddress() throws Exception {
        addToCart(1);

        mvc.perform(post("/api/orders/checkout")
                        .header("Authorization", "Bearer " + token)
                        .contentType(jsonType())
                        .content(json(Map.of("acceptTerms", true))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Le panier refuse une quantité supérieure au stock")
    void cartRejectsQuantityAboveStock() throws Exception {
        mvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(jsonType())
                        .content(json(Map.of(
                                "productId", product.getId().toString(),
                                "variantId", variantId.toString(),
                                "quantity", 99))))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Le montant de la commande inclut les frais de livraison et la TVA")
    void orderAmountsAreCorrect() throws Exception {
        addToCart(1);
        String orderId = checkout();

        Order order = orderRepository.findById(UUID.fromString(orderId)).orElseThrow();

        assertThat(order.getSubtotalAmount()).isEqualByComparingTo("30.00");
        assertThat(order.getShippingAmount())
                .as("Sous le seuil de gratuité, les frais s'appliquent")
                .isGreaterThan(BigDecimal.ZERO);
        assertThat(order.getTotalAmount())
                .isEqualByComparingTo(order.getSubtotalAmount().add(order.getShippingAmount()));
        assertThat(order.getVatAmount())
                .as("La TVA est extraite du total TTC")
                .isGreaterThan(BigDecimal.ZERO)
                .isLessThan(order.getTotalAmount());
    }

    @Test
    @DisplayName("Le paiement confirmé génère une facture unique")
    void paymentGeneratesInvoice() throws Exception {
        addToCart(1);
        String orderId = checkout();
        pay(orderId);
        confirmPayment(orderId);

        assertThat(invoiceRepository.findByOrderId(UUID.fromString(orderId)))
                .as("Une facture doit être émise")
                .isPresent();

        // Un webhook rejoué ne doit pas créer de seconde facture
        confirmPayment(orderId);

        assertThat(invoiceRepository.findAll().stream()
                .filter(i -> i.getOrder().getId().toString().equals(orderId))
                .count())
                .as("Une seule facture par commande")
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Le panier est vidé après la commande")
    void cartIsEmptiedAfterCheckout() throws Exception {
        addToCart(1);
        checkout();

        mvc.perform(get("/api/cart").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty());
    }

    @Test
    @DisplayName("Un client ne peut pas utiliser l'adresse d'un autre client")
    void cannotUseSomeoneElseAddress() throws Exception {
        User other = data.createVerifiedUser("autre-" + UUID.randomUUID() + "@test.local", "motdepasse123");
        Address otherAddress = data.createAddress(other);

        addToCart(1);

        mvc.perform(post("/api/orders/checkout")
                        .header("Authorization", "Bearer " + token)
                        .contentType(jsonType())
                        .content(json(Map.of(
                                "addressId", otherAddress.getId().toString(),
                                "acceptTerms", true))))
                .andExpect(status().isForbidden());
    }

    // ===== Utilitaires =====

    private void addToCart(int quantity) throws Exception {
        mvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(jsonType())
                        .content(json(Map.of(
                                "productId", product.getId().toString(),
                                "variantId", variantId.toString(),
                                "quantity", quantity))))
                .andExpect(status().isOk());
    }

    private String checkout() throws Exception {
        String response = mvc.perform(post("/api/orders/checkout")
                        .header("Authorization", "Bearer " + token)
                        .contentType(jsonType())
                        .content(json(Map.of(
                                "addressId", address.getId().toString(),
                                "acceptTerms", true))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("id").asText();
    }

    private void pay(String orderId) throws Exception {
        mvc.perform(post("/api/orders/" + orderId + "/pay")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    /** Simule le webhook Stripe sans passer par le réseau. */
    private void confirmPayment(String orderId) {
        String sessionId = paymentRepository.findAll().stream()
                .filter(p -> p.getOrder().getId().toString().equals(orderId))
                .map(com.parfum.ecommerce.payment.Payment::getStripeSessionId)
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElseThrow();

        paymentService.confirmPayment(sessionId, "pi_test_" + UUID.randomUUID());
    }
}