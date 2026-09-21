package com.parfum.ecommerce.payment;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.ApiResource;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class StripeWebhookController {

    private final PaymentService paymentService;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    public StripeWebhookController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature
    ) {
        Event event;

        try {
            event = Webhook.constructEvent(payload, signature, webhookSecret);
        } catch (SignatureVerificationException e) {
            return ResponseEntity.badRequest().body("Signature invalide");
        }

        switch (event.getType()) {

            case "checkout.session.completed" -> {
                Session session = extractSession(event);

                System.out.println(">>> WEBHOOK completed, session = "
                        + (session != null ? session.getId() : "NULL"));

                if (session == null) {
                    return ResponseEntity.status(500).body("Session illisible");
                }

                try {
                    paymentService.confirmPayment(session.getId(), session.getPaymentIntent());
                    System.out.println(">>> Paiement confirme avec succes");
                } catch (Exception e) {
                    System.err.println(">>> ECHEC confirmation : " + e.getMessage());
                    e.printStackTrace();
                    // Renvoyer une erreur permet a Stripe de reessayer automatiquement
                    return ResponseEntity.status(500).body("Erreur de traitement");
                }
            }

            case "checkout.session.expired" -> {
                Session session = extractSession(event);

                if (session != null) {
                    try {
                        paymentService.markFailed(session.getId());
                        System.out.println(">>> Session expiree marquee en echec : " + session.getId());
                    } catch (Exception e) {
                        System.err.println(">>> ECHEC marquage expiration : " + e.getMessage());
                    }
                }
            }

            default -> { /* evenement non gere, on l'ignore */ }
        }

        return ResponseEntity.ok("OK");
    }

    /**
     * Extrait l'objet Session d'un evenement Stripe.
     * La deserialisation automatique echoue si la version d'API du compte Stripe
     * ne correspond pas a celle attendue par la bibliotheque stripe-java.
     * Dans ce cas, on deserialise manuellement le JSON brut.
     */
        /**
     * Extrait l'objet Session d'un evenement Stripe.
     * Si la deserialisation automatique echoue (decalage de version d'API),
     * on lit directement les identifiants dans le JSON brut.
     */
    private Session extractSession(Event event) {
        Session session = (Session) event.getDataObjectDeserializer()
                .getObject()
                .orElse(null);

        if (session != null) {
            return session;
        }

        try {
            String rawJson = event.getDataObjectDeserializer().getRawJson();

            String sessionId = extractJsonValue(rawJson, "id");
            String paymentIntent = extractJsonValue(rawJson, "payment_intent");

            if (sessionId == null) {
                return null;
            }

            Session manual = new Session();
            manual.setId(sessionId);
            manual.setPaymentIntent(paymentIntent);
            return manual;

        } catch (Exception e) {
            System.err.println(">>> Extraction manuelle echouee : " + e.getMessage());
            return null;
        }
    }

    /** Lit la valeur textuelle d'une cle de premier niveau dans un JSON. */
    private String extractJsonValue(String json, String key) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("\"" + key + "\"\\s*:\\s*\"([^\"]+)\"")
                .matcher(json);
        return matcher.find() ? matcher.group(1) : null;
    }
}