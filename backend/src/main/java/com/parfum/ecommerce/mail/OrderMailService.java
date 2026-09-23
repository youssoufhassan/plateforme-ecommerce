package com.parfum.ecommerce.mail;

import com.parfum.ecommerce.identity.Address;
import com.parfum.ecommerce.order.Order;
import com.parfum.ecommerce.order.OrderItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Emails liés au cycle de vie d'une commande.
 * Un échec d'envoi ne doit jamais empêcher l'action métier : l'envoi est asynchrone
 * et les erreurs sont seulement journalisées.
 */
@Service
public class OrderMailService {

    private static final Logger log = LoggerFactory.getLogger(OrderMailService.class);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final EmailService emailService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${app.admin-url}")
    private String adminUrl;

    @Value("${app.mail.admin}")
    private String adminEmail;

    public OrderMailService(EmailService emailService) {
        this.emailService = emailService;
    }

    /** Paiement confirmé : accusé de réception au client, alerte à l'administrateur. */
    public void sendOrderReceived(Order order) {
        String email = customerEmail(order);
        if (email == null) return;

        Map<String, Object> vars = baseVars(order);
        vars.put("accountUrl", frontendUrl + "/account");

        emailService.send(email, "Commande #" + shortId(order) + " bien reçue", "order-received", vars);

        sendAdminAlert(order);
    }

    public void sendOrderPreparing(Order order) {
        String email = customerEmail(order);
        if (email == null) return;

        Map<String, Object> vars = new HashMap<>();
        vars.put("firstName", firstName(order));
        vars.put("orderNumber", shortId(order));
        vars.put("accountUrl", frontendUrl + "/account");

        emailService.send(email, "Commande #" + shortId(order) + " en préparation", "order-preparing", vars);
    }

    public void sendOrderShipped(Order order, String carrier, String trackingNumber) {
        String email = customerEmail(order);
        if (email == null) return;

        Map<String, Object> vars = new HashMap<>();
        vars.put("firstName", firstName(order));
        vars.put("orderNumber", shortId(order));
        vars.put("carrier", carrier != null ? carrier : "Transporteur");
        vars.put("trackingNumber", trackingNumber != null ? trackingNumber : "—");
        vars.put("address", addressLines(order.getAddress()));
        vars.put("accountUrl", frontendUrl + "/account");

        emailService.send(email, "Commande #" + shortId(order) + " expédiée", "order-shipped", vars);
    }

    public void sendOrderDelivered(Order order) {
        String email = customerEmail(order);
        if (email == null) return;

        Map<String, Object> vars = new HashMap<>();
        vars.put("firstName", firstName(order));
        vars.put("orderNumber", shortId(order));
        vars.put("shopUrl", frontendUrl);

        emailService.send(email, "Commande #" + shortId(order) + " livrée", "order-delivered", vars);
    }

    public void sendOrderCancelled(Order order, boolean wasPaid) {
        String email = customerEmail(order);
        if (email == null) return;

        Map<String, Object> vars = new HashMap<>();
        vars.put("firstName", firstName(order));
        vars.put("orderNumber", shortId(order));
        vars.put("wasPaid", wasPaid);
        vars.put("total", money(order.getTotalAmount()));

        emailService.send(email, "Commande #" + shortId(order) + " annulée", "order-cancelled", vars);
    }

    private void sendAdminAlert(Order order) {
        if (adminEmail == null || adminEmail.isBlank()) {
            log.info("Aucune adresse administrateur configurée : alerte de commande non envoyée");
            return;
        }

        Map<String, Object> vars = baseVars(order);
        vars.put("customerName", customerName(order));
        vars.put("customerEmail", customerEmail(order));
        vars.put("adminUrl", adminUrl + "/orders");
        vars.put("hasDropship", order.getItems().stream()
                .anyMatch(i -> "DROPSHIP".equals(i.getProduct().getFulfillmentType())));

        emailService.send(adminEmail, "Nouvelle commande #" + shortId(order)
                + " — " + money(order.getTotalAmount()), "admin-new-order", vars);
    }

    private Map<String, Object> baseVars(Order order) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("firstName", firstName(order));
        vars.put("orderNumber", shortId(order));
        vars.put("orderDate", order.getCreatedAt().format(DATE));
        vars.put("items", itemLines(order));
        vars.put("shipping", money(order.getShippingAmount()));
        vars.put("total", money(order.getTotalAmount()));
        vars.put("address", addressLines(order.getAddress()));
        return vars;
    }

    private List<Map<String, String>> itemLines(Order order) {
        List<Map<String, String>> lines = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            Map<String, String> line = new HashMap<>();
            line.put("name", item.getDisplayName());
            line.put("quantity", String.valueOf(item.getQuantity()));
            line.put("total", money(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))));
            lines.add(line);
        }
        return lines;
    }

    private List<String> addressLines(Address a) {
        if (a == null) return List.of();

        List<String> lines = new ArrayList<>();
        String name = ((a.getFirstName() != null ? a.getFirstName() : "") + " "
                + (a.getLastName() != null ? a.getLastName() : "")).trim();
        if (!name.isEmpty()) lines.add(name);

        lines.add(a.getStreet());
        if (a.getComplement() != null && !a.getComplement().isBlank()) lines.add(a.getComplement());
        lines.add(a.getPostalCode() + " " + a.getCity());
        lines.add(a.getCountryCode());
        return lines;
    }

    private String customerEmail(Order order) {
        return order.getUser() != null ? order.getUser().getEmail() : null;
    }

    private String firstName(Order order) {
        if (order.getAddress() != null && order.getAddress().getFirstName() != null) {
            return order.getAddress().getFirstName();
        }
        if (order.getUser() != null && order.getUser().getFirstName() != null) {
            return order.getUser().getFirstName();
        }
        return "";
    }

    private String customerName(Order order) {
        Address a = order.getAddress();
        if (a != null && a.getFirstName() != null) {
            return (a.getFirstName() + " " + (a.getLastName() != null ? a.getLastName() : "")).trim();
        }
        return customerEmail(order) != null ? customerEmail(order) : "Client";
    }

    private String shortId(Order order) {
        return order.getId().toString().substring(0, 8).toUpperCase();
    }

    private String money(BigDecimal amount) {
        if (amount == null) amount = BigDecimal.ZERO;
        return amount.setScale(2, RoundingMode.HALF_UP).toString().replace(".", ",") + " €";
    }
}