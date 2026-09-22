package com.parfum.ecommerce.order;

import com.parfum.ecommerce.shipping.ShippingZone;
import com.parfum.ecommerce.shipping.ShippingZoneRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Calcule les montants d'une commande.
 * Les prix produits sont TTC : la TVA est extraite, jamais ajoutée.
 */
@Service
public class PricingService {

    private final ShippingZoneRepository shippingZoneRepository;

    public PricingService(ShippingZoneRepository shippingZoneRepository) {
        this.shippingZoneRepository = shippingZoneRepository;
    }

    public ShippingZone resolveZone(String countryCode) {
        return shippingZoneRepository.findByCountryCode(countryCode)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Nous ne livrons pas encore dans ce pays : " + countryCode));
    }

    public PriceBreakdown calculate(BigDecimal subtotal, String countryCode) {
        ShippingZone zone = resolveZone(countryCode);

        BigDecimal shipping = computeShipping(subtotal, zone);
        BigDecimal total = subtotal.add(shipping).setScale(2, RoundingMode.HALF_UP);
        BigDecimal vat = extractVat(total, zone.getVatRate());

        return new PriceBreakdown(
                subtotal.setScale(2, RoundingMode.HALF_UP),
                shipping,
                vat,
                zone.getVatRate(),
                total,
                zone.getFreeThreshold(),
                zone.getName()
        );
    }

       private BigDecimal computeShipping(BigDecimal subtotal, ShippingZone zone) {
        if (subtotal.signum() == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        if (zone.getFreeThreshold() != null && subtotal.compareTo(zone.getFreeThreshold()) >= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return zone.getFlatRate().setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Extrait la TVA d'un montant TTC.
     * Exemple à 20 % : sur 120 € TTC, la TVA vaut 120 - (120 / 1,20) = 20 €.
     */
    private BigDecimal extractVat(BigDecimal totalTtc, BigDecimal vatRate) {
        BigDecimal divisor = BigDecimal.ONE.add(
                vatRate.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
        BigDecimal ht = totalTtc.divide(divisor, 2, RoundingMode.HALF_UP);
        return totalTtc.subtract(ht).setScale(2, RoundingMode.HALF_UP);
    }

    public record PriceBreakdown(
            BigDecimal subtotal,
            BigDecimal shipping,
            BigDecimal vat,
            BigDecimal vatRate,
            BigDecimal total,
            BigDecimal freeShippingThreshold,
            String zoneName
    ) {
        public BigDecimal amountUntilFreeShipping() {
            if (freeShippingThreshold == null) return BigDecimal.ZERO;
            return freeShippingThreshold.subtract(subtotal).max(BigDecimal.ZERO);
        }
    }
}