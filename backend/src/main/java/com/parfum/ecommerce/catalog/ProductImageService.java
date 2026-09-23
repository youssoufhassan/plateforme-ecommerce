package com.parfum.ecommerce.catalog;

import com.parfum.ecommerce.catalog.dto.ProductImageResponse;
import com.parfum.ecommerce.storage.ImageStorage;
import com.parfum.ecommerce.storage.ImageValidator;
import com.parfum.ecommerce.storage.StoredImage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ProductImageService {

    private static final int MAX_IMAGES_PER_PRODUCT = 10;

    private final ProductRepository productRepository;
    private final ProductImageRepository imageRepository;
    private final ImageStorage imageStorage;

    @Value("${app.images.max-size-mb}")
    private int maxSizeMb;

    public ProductImageService(ProductRepository productRepository,
                                ProductImageRepository imageRepository,
                                ImageStorage imageStorage) {
        this.productRepository = productRepository;
        this.imageRepository = imageRepository;
        this.imageStorage = imageStorage;
    }

    @Transactional(readOnly = true)
    public List<ProductImageResponse> list(UUID productId) {
        Product product = findProduct(productId);
        return imageRepository.findByProductIdOrderByPositionAsc(productId).stream()
                .map(image -> toResponse(image, product))
                .toList();
    }

    /** Envoi d'une ou plusieurs images. La première devient l'image principale si le produit n'en a pas. */
    @Transactional
    public List<ProductImageResponse> upload(UUID productId, MultipartFile[] files) {
        Product product = findProduct(productId);

        long existing = imageRepository.countByProductId(productId);
        if (existing + files.length > MAX_IMAGES_PER_PRODUCT) {
            throw new IllegalStateException(
                    "Maximum " + MAX_IMAGES_PER_PRODUCT + " images par produit (" + existing + " déjà présentes)");
        }

        int position = (int) existing;
        List<ProductImageResponse> created = new ArrayList<>();

        for (MultipartFile file : files) {
            ImageValidator.validate(file, maxSizeMb);

            byte[] content;
            try {
                content = file.getBytes();
            } catch (java.io.IOException e) {
                throw new IllegalStateException("Impossible de lire " + file.getOriginalFilename());
            }

            StoredImage stored = imageStorage.store(content, file.getOriginalFilename(), file.getContentType());

            ProductImage image = new ProductImage(
                    product, stored.url(), product.getName(), position++, stored.reference());

            imageRepository.save(image);
            product.getImages().add(image);

            // Première image d'un produit sans visuel : elle devient l'image principale
            if (product.getImageUrl() == null || product.getImageUrl().isBlank()) {
                product.setImageUrl(stored.url());
                productRepository.save(product);
            }

            created.add(toResponse(image, product));
        }

        return created;
    }

    /** Définit l'image principale, celle affichée dans le catalogue. */
    @Transactional
    public List<ProductImageResponse> setMain(UUID productId, UUID imageId) {
        Product product = findProduct(productId);
        ProductImage image = findImage(imageId, productId);

        product.setImageUrl(image.getUrl());
        productRepository.save(product);

        return list(productId);
    }

    /** Réordonne la galerie selon l'ordre des identifiants fournis. */
    @Transactional
    public List<ProductImageResponse> reorder(UUID productId, List<UUID> orderedIds) {
        findProduct(productId);

        List<ProductImage> images = imageRepository.findByProductIdOrderByPositionAsc(productId);

        if (orderedIds == null || orderedIds.size() != images.size()) {
            throw new IllegalArgumentException("La liste doit contenir toutes les images du produit");
        }

        int position = 0;
        for (UUID id : orderedIds) {
            ProductImage image = images.stream()
                    .filter(i -> i.getId().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Image inconnue pour ce produit"));

            image.setPosition(position++);
            imageRepository.save(image);
        }

        return list(productId);
    }

    /** Supprime une image, du produit et du stockage. */
    @Transactional
    public List<ProductImageResponse> delete(UUID productId, UUID imageId) {
        Product product = findProduct(productId);
        ProductImage image = findImage(imageId, productId);

        boolean wasMain = image.getUrl().equals(product.getImageUrl());

        product.getImages().remove(image);
        imageRepository.delete(image);

        if (image.getStorageReference() != null) {
            imageStorage.delete(image.getStorageReference());
        }

        // Si l'image principale disparaît, la suivante prend sa place
        if (wasMain) {
            List<ProductImage> remaining = imageRepository.findByProductIdOrderByPositionAsc(productId);
            product.setImageUrl(remaining.isEmpty() ? null : remaining.get(0).getUrl());
        }

        productRepository.save(product);
        return list(productId);
    }

    private ProductImageResponse toResponse(ProductImage image, Product product) {
        return new ProductImageResponse(
                image.getId(),
                image.getUrl(),
                image.getAltText(),
                image.getPosition() != null ? image.getPosition() : 0,
                image.getUrl().equals(product.getImageUrl())
        );
    }

    private Product findProduct(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Produit introuvable"));
    }

    private ProductImage findImage(UUID imageId, UUID productId) {
        ProductImage image = imageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Image introuvable"));

        if (!image.getProduct().getId().equals(productId)) {
            throw new IllegalArgumentException("Cette image n'appartient pas à ce produit");
        }
        return image;
    }
}