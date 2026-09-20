package com.parfum.ecommerce.invoice;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.parfum.ecommerce.order.OrderItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class InvoicePdfGenerator {

    private final TemplateEngine templateEngine;

    @Value("${app.company.name}") private String companyName;
    @Value("${app.company.legal-form}") private String legalForm;
    @Value("${app.company.address}") private String companyAddress;
    @Value("${app.company.postal-code}") private String companyPostalCode;
    @Value("${app.company.city}") private String companyCity;
    @Value("${app.company.country}") private String companyCountry;
    @Value("${app.company.siret}") private String siret;
    @Value("${app.company.vat-number}") private String vatNumber;
    @Value("${app.company.email}") private String companyEmail;

    public InvoicePdfGenerator(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public byte[] generate(Invoice invoice) {
        Context context = new Context();
        context.setVariables(buildVariables(invoice));

        String html = templateEngine.process("invoice/invoice", context);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(out);
            builder.run();
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Impossible de générer la facture PDF : " + e.getMessage());
        }
    }

    private Map<String, Object> buildVariables(Invoice invoice) {
        Map<String, Object> vars = new HashMap<>();

        vars.put("companyName", companyName);
        vars.put("legalForm", legalForm);
        vars.put("companyAddress", companyAddress);
        vars.put("companyPostalCode", companyPostalCode);
        vars.put("companyCity", companyCity);
        vars.put("companyCountry", companyCountry);
        vars.put("siret", siret);
        vars.put("vatNumber", vatNumber);
        vars.put("companyEmail", companyEmail);

        vars.put("invoiceNumber", invoice.getInvoiceNumber());
        vars.put("issuedAt", invoice.getIssuedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        vars.put("customerName", invoice.getCustomerName());
        vars.put("customerEmail", invoice.getCustomerEmail());
        vars.put("billingAddress", invoice.getBillingAddress().split("\n"));

        List<Map<String, String>> lines = new ArrayList<>();
        BigDecimal vatDivisor = BigDecimal.ONE.add(
                invoice.getVatRate().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));

        for (OrderItem item : invoice.getOrder().getItems()) {
            BigDecimal lineTtc = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            BigDecimal lineHt = lineTtc.divide(vatDivisor, 2, RoundingMode.HALF_UP);
            BigDecimal unitHt = item.getUnitPrice().divide(vatDivisor, 2, RoundingMode.HALF_UP);

            Map<String, String> line = new HashMap<>();
            line.put("name", item.getProduct().getName());
            line.put("quantity", String.valueOf(item.getQuantity()));
            line.put("unitHt", format(unitHt));
            line.put("totalHt", format(lineHt));
            lines.add(line);
        }
        vars.put("lines", lines);

        BigDecimal subtotalHt = invoice.getSubtotalAmount().divide(vatDivisor, 2, RoundingMode.HALF_UP);
        BigDecimal shippingHt = invoice.getShippingAmount().divide(vatDivisor, 2, RoundingMode.HALF_UP);
        BigDecimal totalHt = invoice.getTotalAmount().subtract(invoice.getVatAmount());

        vars.put("subtotalHt", format(subtotalHt));
        vars.put("shippingHt", format(shippingHt));
        vars.put("shippingTtc", format(invoice.getShippingAmount()));
        vars.put("totalHt", format(totalHt));
        vars.put("vatRate", invoice.getVatRate().setScale(0, RoundingMode.HALF_UP).toString());
        vars.put("vatAmount", format(invoice.getVatAmount()));
        vars.put("totalTtc", format(invoice.getTotalAmount()));

        return vars;
    }

    private String format(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).toString().replace(".", ",") + " €";
    }
}