package com.parfum.ecommerce.supplier;

import com.parfum.ecommerce.catalog.Category;
import com.parfum.ecommerce.catalog.CategoryRepository;
import com.parfum.ecommerce.catalog.Product;
import com.parfum.ecommerce.catalog.ProductImage;
import com.parfum.ecommerce.catalog.ProductImageRepository;
import com.parfum.ecommerce.catalog.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FragellaImportService {

    private final FragellaClient fragellaClient;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final CategoryRepository categoryRepository;

    public FragellaImportService(
            FragellaClient fragellaClient,
            ProductRepository productRepository,
            ProductImageRepository productImageRepository,
            CategoryRepository categoryRepository
    ) {
        this.fragellaClient = fragellaClient;
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public Product importProduct(String search) {

        List<FragellaProduct> results =
                fragellaClient.searchFragrances(search, 1);

        if (results.isEmpty()) {
            throw new IllegalArgumentException(
                    "Aucun parfum trouvé dans Fragella pour : " + search
            );
        }

        FragellaProduct source = results.get(0);

        if (source.getName() == null || source.getName().isBlank()) {
            throw new IllegalArgumentException(
                    "Le produit Fragella ne possède pas de nom."
            );
        }

        if (source.getPrice() == null || source.getPrice().signum() <= 0) {
            throw new IllegalArgumentException(
                    "Le produit Fragella ne possède pas de prix valide."
            );
        }

        Product product = new Product();

        product.setName(source.getName());
        product.setPrice(source.getPrice());
        product.setStockQuantity(10);
        product.setActive(true);
        product.setDescription(buildDescription(source));

        Category category = findOrCreateCategory(source.getGender());
        product.setCategory(category);

        /*
         * Récupération de toutes les images disponibles.
         */
        List<String> imageUrls = collectImageUrls(source);

        /*
         * Première image = image principale.
         */
        if (!imageUrls.isEmpty()) {
            product.setImageUrl(imageUrls.get(0));
        }

        /*
         * Sauvegarde du produit.
         */
        Product savedProduct = productRepository.save(product);

        /*
         * Sauvegarde réelle des images dans product_images.
         */
        saveProductImages(savedProduct, imageUrls);

        return savedProduct;
    }

    private void saveProductImages(
            Product product,
            List<String> imageUrls
    ) {
        if (imageUrls.isEmpty()) {
            return;
        }

        List<ProductImage> images = new ArrayList<>();

        for (int i = 0; i < imageUrls.size(); i++) {

            ProductImage image = new ProductImage();

            image.setProduct(product);
            image.setUrl(imageUrls.get(i));
            image.setAltText(
                    product.getName() + " - image " + (i + 1)
            );
            image.setPosition(i);

            images.add(image);
        }

        productImageRepository.saveAll(images);
    }

    /**
     * Récupère les différentes sources d'images fournies par Fragella.
     *
     * L'ordre est volontaire :
     * 1. Image URL
     * 2. Image URL Transparent
     * 3. Image Fallbacks
     *
     * LinkedHashSet permet de supprimer les doublons
     * tout en conservant l'ordre.
     */
    private List<String> collectImageUrls(FragellaProduct product) {

        Set<String> urls = new LinkedHashSet<>();

        addIfValid(urls, product.getImageUrl());
        addIfValid(urls, product.getImageUrlTransparent());

        if (product.getImageFallbacks() != null) {

            for (String fallback : product.getImageFallbacks()) {
                addIfValid(urls, fallback);
            }
        }

        return new ArrayList<>(urls);
    }

    private void addIfValid(
            Set<String> urls,
            String url
    ) {
        if (url == null || url.isBlank()) {
            return;
        }

        String cleanUrl = url.trim();

        if (!cleanUrl.startsWith("http://")
                && !cleanUrl.startsWith("https://")) {
            return;
        }

        urls.add(cleanUrl);
    }

    private Category findOrCreateCategory(String gender) {

        String categoryName = normalizeCategoryName(gender);

        return categoryRepository.findAll()
                .stream()
                .filter(category ->
                        category.getName().equalsIgnoreCase(categoryName)
                )
                .findFirst()
                .orElseGet(() -> {

                    Category category = new Category();
                    category.setName(categoryName);

                    return categoryRepository.save(category);
                });
    }

    private String normalizeCategoryName(String gender) {

        if (gender == null || gender.isBlank()) {
            return "Unisexe";
        }

        return switch (gender.toLowerCase(Locale.ROOT)) {

            case "men", "male", "homme" ->
                    "Homme";

            case "women", "female", "femme" ->
                    "Femme";

            case "unisex", "unisexes", "unisexe" ->
                    "Unisexe";

            default ->
                    "Unisexe";
        };
    }

    private String buildDescription(FragellaProduct product) {

        StringBuilder description = new StringBuilder();

        if (product.getBrand() != null
                && !product.getBrand().isBlank()) {

            description.append(product.getBrand());
        }

        if (product.getYear() != null) {

            appendSeparator(description);

            description.append("Lancé en ")
                    .append(product.getYear());
        }

        if (product.getOilType() != null
                && !product.getOilType().isBlank()) {

            appendSeparator(description);

            description.append(product.getOilType());
        }

        if (product.getGender() != null
                && !product.getGender().isBlank()) {

            appendSeparator(description);

            description.append(
                    formatGender(product.getGender())
            );
        }

        if (product.getLongevity() != null
                && !product.getLongevity().isBlank()) {

            appendSeparator(description);

            description.append("Longévité : ")
                    .append(product.getLongevity());
        }

        if (product.getSillage() != null
                && !product.getSillage().isBlank()) {

            appendSeparator(description);

            description.append("Sillage : ")
                    .append(product.getSillage());
        }

        if (product.getRating() != null) {

            appendSeparator(description);

            description.append("Note : ")
                    .append(product.getRating())
                    .append("/5");
        }

        if (product.getMainAccords() != null
                && !product.getMainAccords().isEmpty()) {

            appendSeparator(description);

            description.append("Accords : ")
                    .append(
                            product.getMainAccords()
                                    .stream()
                                    .filter(value ->
                                            value != null
                                                    && !value.isBlank()
                                    )
                                    .collect(
                                            Collectors.joining(", ")
                                    )
                    );
        }

        return description.toString();
    }

    private String formatGender(String gender) {

        return switch (gender.toLowerCase(Locale.ROOT)) {

            case "men", "male" ->
                    "Homme";

            case "women", "female" ->
                    "Femme";

            case "unisex", "unisexes", "unisexe" ->
                    "Unisexe";

            default ->
                    gender;
        };
    }

    private void appendSeparator(StringBuilder builder) {

        if (!builder.isEmpty()) {
            builder.append(" · ");
        }
    }
}