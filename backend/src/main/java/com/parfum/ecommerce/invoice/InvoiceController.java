package com.parfum.ecommerce.invoice;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    /** Téléchargement de la facture par le client propriétaire de la commande. */
    @GetMapping("/orders/{orderId}/invoice")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable UUID orderId, Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        byte[] pdf = invoiceService.getPdf(orderId, auth.getName(), isAdmin);
        String number = invoiceService.getByOrderId(orderId).getInvoiceNumber();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"facture-" + number + ".pdf\"")
                .body(pdf);
    }
}