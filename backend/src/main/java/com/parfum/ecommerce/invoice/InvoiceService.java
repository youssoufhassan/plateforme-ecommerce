package com.parfum.ecommerce.invoice;

import com.parfum.ecommerce.identity.Address;
import com.parfum.ecommerce.order.Order;
import com.parfum.ecommerce.order.OrderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.UUID;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final OrderRepository orderRepository;
    private final InvoicePdfGenerator pdfGenerator;

    @Value("${app.invoice.prefix}")
    private String prefix;

    public InvoiceService(InvoiceRepository invoiceRepository, OrderRepository orderRepository,
                           InvoicePdfGenerator pdfGenerator) {
        this.invoiceRepository = invoiceRepository;
        this.orderRepository = orderRepository;
        this.pdfGenerator = pdfGenerator;
    }

    /** Émet la facture d'une commande payée. Idempotent : ne crée jamais de doublon. */
    @Transactional
    public Invoice issueForOrder(Order order) {
        if (invoiceRepository.existsByOrderId(order.getId())) {
            return invoiceRepository.findByOrderId(order.getId()).orElseThrow();
        }

        Long sequence = invoiceRepository.nextSequenceNumber();

        Invoice invoice = new Invoice();
        invoice.setOrder(order);
        invoice.setSequenceNumber(sequence);
        invoice.setInvoiceNumber(String.format("%s-%d-%05d", prefix, Year.now().getValue(), sequence));

        invoice.setSubtotalAmount(order.getSubtotalAmount());
        invoice.setShippingAmount(order.getShippingAmount());
        invoice.setVatAmount(order.getVatAmount());
        invoice.setVatRate(order.getVatRate());
        invoice.setTotalAmount(order.getTotalAmount());

        invoice.setCustomerName(buildCustomerName(order));
        invoice.setCustomerEmail(order.getUser() != null ? order.getUser().getEmail() : "");
        invoice.setBillingAddress(buildAddress(order.getAddress()));

        return invoiceRepository.save(invoice);
    }

    public byte[] getPdf(UUID orderId, String userEmail, boolean isAdmin) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Commande introuvable"));

        if (!isAdmin) {
            if (order.getUser() == null || !order.getUser().getEmail().equals(userEmail)) {
                throw new SecurityException("Cette facture ne vous appartient pas");
            }
        }

        Invoice invoice = invoiceRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalStateException(
                        "Aucune facture n'a encore été émise pour cette commande"));

        return pdfGenerator.generate(invoice);
    }

    public Invoice getByOrderId(UUID orderId) {
        return invoiceRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalStateException("Aucune facture pour cette commande"));
    }

    private String buildCustomerName(Order order) {
        Address address = order.getAddress();
        if (address != null && address.getFirstName() != null) {
            return (address.getFirstName() + " " + 
                    (address.getLastName() != null ? address.getLastName() : "")).trim();
        }
        if (order.getUser() != null) {
            String first = order.getUser().getFirstName();
            String last = order.getUser().getLastName();
            String name = ((first != null ? first : "") + " " + (last != null ? last : "")).trim();
            return name.isEmpty() ? order.getUser().getEmail() : name;
        }
        return "Client";
    }

    private String buildAddress(Address a) {
        if (a == null) return "";
        StringBuilder sb = new StringBuilder();
        sb.append(a.getStreet()).append("\n");
        if (a.getComplement() != null && !a.getComplement().isBlank()) {
            sb.append(a.getComplement()).append("\n");
        }
        sb.append(a.getPostalCode()).append(" ").append(a.getCity()).append("\n");
        sb.append(a.getCountryCode());
        return sb.toString();
    }
}