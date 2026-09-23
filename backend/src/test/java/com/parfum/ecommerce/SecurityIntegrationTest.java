package com.parfum.ecommerce;

import com.parfum.ecommerce.identity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestConfig.class)
@DisplayName("Sécurité des accès")
class SecurityIntegrationTest extends AbstractIntegrationTest {

    @Autowired private TestDataHelper data;

    @MockitoBean private com.parfum.ecommerce.mail.EmailService emailService;

    private String clientToken;

    @BeforeEach
    void setUp() throws Exception {
        String email = "client-sec-" + UUID.randomUUID() + "@test.local";
        data.createVerifiedUser(email, "motdepasse123");
        clientToken = login(email, "motdepasse123");
    }

    @Test
    @DisplayName("Le catalogue est accessible sans authentification")
    void catalogIsPublic() throws Exception {
        mvc.perform(get("/api/products")).andExpect(status().isOk());
        mvc.perform(get("/api/products/search")).andExpect(status().isOk());
        mvc.perform(get("/api/categories")).andExpect(status().isOk());
        mvc.perform(get("/api/legal")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Le panier exige une authentification")
    void cartRequiresAuthentication() throws Exception {
        mvc.perform(get("/api/cart")).andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Un client ne peut pas accéder aux endpoints d'administration")
    void clientCannotAccessAdmin() throws Exception {
        mvc.perform(get("/api/admin/customers").header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isForbidden());

        mvc.perform(get("/api/products/admin").header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isForbidden());

        mvc.perform(get("/api/admin/dashboard/stats").header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Un client ne peut pas consulter les commandes de tous les clients")
    void clientCannotListAllOrders() throws Exception {
        mvc.perform(get("/api/orders/admin").header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Un client ne peut pas modifier le statut d'une commande")
    void clientCannotChangeOrderStatus() throws Exception {
        mvc.perform(put("/api/orders/" + UUID.randomUUID() + "/status")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(jsonType())
                        .content(json(Map.of("status", "DELIVERED"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Un jeton invalide est rejeté")
    void invalidTokenIsRejected() throws Exception {
        mvc.perform(get("/api/me").header("Authorization", "Bearer jeton.invalide.xxx"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Un compte non vérifié ne peut pas commander")
    void unverifiedUserCannotOrder() throws Exception {
        String email = "nonverifie-" + UUID.randomUUID() + "@test.local";

        mvc.perform(post("/api/auth/register")
                        .contentType(jsonType())
                        .content(json(Map.of("email", email, "password", "motdepasse123"))))
                .andExpect(status().isOk());

        String token = login(email, "motdepasse123");

        mvc.perform(post("/api/orders/checkout")
                        .header("Authorization", "Bearer " + token)
                        .contentType(jsonType())
                        .content(json(Map.of("addressId", UUID.randomUUID().toString(), "acceptTerms", true))))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("La demande de mot de passe oublié ne révèle pas si le compte existe")
    void forgotPasswordDoesNotRevealAccounts() throws Exception {
        mvc.perform(post("/api/auth/forgot-password")
                        .contentType(jsonType())
                        .content(json(Map.of("email", "inconnu-" + UUID.randomUUID() + "@test.local"))))
                .andExpect(status().isOk());
    }

    private String login(String email, String password) throws Exception {
        String response = mvc.perform(post("/api/auth/login")
                        .contentType(jsonType())
                        .content(json(Map.of("email", email, "password", password))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("token").asText();
    }
}