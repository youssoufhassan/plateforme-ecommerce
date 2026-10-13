package com.parfum.ecommerce.catalog;

import com.parfum.ecommerce.catalog.dto.ProductOptionResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class VariantOptionService {

    /** Au-delà, la gestion devient ingérable depuis l'administration. */
    private static final int MAX_AXES = 3;
    private static final int MAX_COMBINATIONS = 100;

    private final ProductRepository productRepository;
    private final ProductOptionRepository optionRepository;
    private final ProductVariantRepository variantRepository;

    public VariantOptionService(ProductRepository productRepository,
                                 ProductOptionRepository optionRepository,
                                 ProductVariantRepository variantRepository) {
        this.productRepository = productRepository;
        this.optionRepository = optionRepository;
        this.variantRepository = variantRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductOptionResponse> options(UUID productId) {
        return optionRepository.findByProductIdOrderByPositionAsc(productId).stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Définit les axes d'un produit et leurs valeurs.
     * Remplace l'existant : les variantes devront être régénérées.
     */
    @Transactional
    public List<ProductOptionResponse> defineOptions(UUID productId,
                                                      List<Map<String, Object>> axes) {
        Product product = find(productId);

        if (axes == null || axes.isEmpty()) {
            throw new IllegalArgumentException("Au moins un axe est requis");
        }
        if (axes.size() > MAX_AXES) {
            throw new IllegalArgumentException("Maximum " + MAX_AXES + " axes par produit");
        }

        product.getOptions().clear();

        int position = 0;
        for (Map<String, Object> axis : axes) {
            String name = (String) axis.get("name");

            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Chaque axe doit avoir un nom");
            }

            @SuppressWarnings("unchecked")
            List<String> values = (List<String>) axis.get("values");

            if (values == null || values.isEmpty()) {
                throw new IllegalArgumentException("L'axe « " + name + " » n'a aucune valeur");
            }

            ProductOption option = new ProductOption(product, name.trim(), position++);

            int valuePosition = 0;
            for (String value : new LinkedHashSet<>(values)) {
                option.getValues().add(new ProductOptionValue(option, value.trim(), valuePosition++));
            }

            product.getOptions().add(option);
        }

        productRepository.save(product);

        return options(productId);
    }

    /**
     * Crée toutes les combinaisons manquantes.
     * Sans cela, saisir 5 tailles × 6 couleurs demanderait 30 créations à la main.
     */
    @Transactional
    public Map<String, Object> generateVariants(UUID productId,
                                                 BigDecimal price,
                                                 BigDecimal costPrice,
                                                 Integer stock) {
        Product product = find(productId);
        List<ProductOption> options = product.getOptions();

        if (options.isEmpty()) {
            throw new IllegalStateException("Définissez d'abord les axes de variation");
        }
        if (price == null || price.signum() <= 0) {
            throw new IllegalArgumentException("Un prix de départ est requis");
        }

        List<List<ProductOptionValue>> combinations = cartesianProduct(options);

        if (combinations.size() > MAX_COMBINATIONS) {
            throw new IllegalArgumentException(
                    combinations.size() + " combinaisons : réduisez le nombre de valeurs "
                    + "(maximum " + MAX_COMBINATIONS + ")");
        }

        // Les combinaisons déjà présentes ne sont pas recréées
        var existing = variantRepository.findByProductIdOrderByPositionAsc(productId).stream()
                .map(v -> v.getOptionValues().stream()
                        .map(ProductOptionValue::getId)
                        .sorted()
                        .toList())
                .collect(java.util.stream.Collectors.toSet());

        int created = 0;
        int position = product.getVariants().size();

        for (List<ProductOptionValue> combination : combinations) {
            var signature = combination.stream()
                    .map(ProductOptionValue::getId)
                    .sorted()
                    .toList();

            if (existing.contains(signature)) continue;

            ProductVariant variant = new ProductVariant();
            variant.setProduct(product);
            variant.setPrice(price);
            variant.setCostPrice(costPrice);
            variant.setStockQuantity(stock != null ? stock : 0);
            variant.setActive(true);
            variant.setPosition(position++);
            variant.setOptionValues(new LinkedHashSet<>(combination));

            // Le libellé reste lisible dans le panier et les commandes
            variant.setLabel(combination.stream()
                    .map(ProductOptionValue::getValue)
                    .reduce((a, b) -> a + " / " + b)
                    .orElse("Standard"));

            variantRepository.save(variant);
            created++;
        }

        syncProductColumns(product);
        productRepository.save(product);

        return Map.of(
                "combinations", combinations.size(),
                "created", created,
                "skipped", combinations.size() - created,
                "message", created + " variante(s) créée(s)");
    }

    // ===== Utilitaires =====

    /** Toutes les combinaisons possibles entre les axes. */
    private List<List<ProductOptionValue>> cartesianProduct(List<ProductOption> options) {
        List<List<ProductOptionValue>> result = new ArrayList<>();
        result.add(new ArrayList<>());

        for (ProductOption option : options) {
            List<List<ProductOptionValue>> next = new ArrayList<>();

            for (List<ProductOptionValue> partial : result) {
                for (ProductOptionValue value : option.getValues()) {
                    List<ProductOptionValue> extended = new ArrayList<>(partial);
                    extended.add(value);
                    next.add(extended);
                }
            }
            result = next;
        }
        return result;
    }

    /** Garde le prix et le stock du produit cohérents avec ses variantes. */
    private void syncProductColumns(Product product) {
        var active = product.getActiveVariants();

        active.stream()
                .map(ProductVariant::getPrice)
                .min(BigDecimal::compareTo)
                .ifPresent(product::setPrice);

        product.setStockQuantity(active.stream()
                .mapToInt(v -> v.getStockQuantity() != null ? v.getStockQuantity() : 0)
                .sum());
    }

    private ProductOptionResponse toResponse(ProductOption option) {
        return new ProductOptionResponse(
                option.getId(),
                option.getName(),
                option.getPosition(),
                option.getValues().stream()
                        .map(v -> new ProductOptionResponse.OptionValue(
                                v.getId(), v.getValue(), v.getPosition()))
                        .toList());
    }

    private Product find(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produit introuvable"));
    }
}