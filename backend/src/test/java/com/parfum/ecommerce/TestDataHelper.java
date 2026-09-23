package com.parfum.ecommerce;

import com.parfum.ecommerce.catalog.*;
import com.parfum.ecommerce.identity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/** Crée les données nécessaires aux tests. */
@Component
public class TestDataHelper {

    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private AddressRepository addressRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private ProductVariantRepository variantRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Transactional
    public User createVerifiedUser(String email, String rawPassword) {
        User user = new User(email, passwordEncoder.encode(rawPassword));
        user.setFirstName("Test");
        user.setLastName("Client");
        user.setEmailVerified(true);
        user.setEmailVerifiedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Transactional
    public User createAdmin(String email, String rawPassword) {
        User admin = createVerifiedUser(email, rawPassword);
        roleRepository.findByName("ADMIN").ifPresent(role -> {
            admin.getRoles().add(role);
            userRepository.save(admin);
        });
        return admin;
    }

    @Transactional
    public Address createAddress(User user) {
        Address address = new Address();
        address.setUser(user);
        address.setFirstName("Test");
        address.setLastName("Client");
        address.setStreet("1 rue de Test");
        address.setCity("Paris");
        address.setPostalCode("75001");
        address.setCountryCode("FR");
        address.setCountry("FR");
        return addressRepository.save(address);
    }

    @Transactional
    public Product createProduct(String name, BigDecimal price, int stock) {
        Category category = categoryRepository.findByName("Parfums").orElseGet(() -> {
            Category c = new Category();
            c.setName("Parfums");
            return categoryRepository.save(c);
        });

        Product product = new Product();
        product.setName(name);
        product.setDescription("Produit de test");
        product.setPrice(price);
        product.setStockQuantity(stock);
        product.setActive(true);
        product.setCategory(category);
        product.setFulfillmentType("OWN_STOCK");
        productRepository.save(product);

        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setLabel("Standard");
        variant.setPrice(price);
        variant.setStockQuantity(stock);
        variant.setActive(true);
        variantRepository.save(variant);

        product.getVariants().add(variant);
        return productRepository.save(product);
    }

    @Transactional
    public UUID firstVariantId(Product product) {
        return variantRepository.findByProductIdOrderByPositionAsc(product.getId()).get(0).getId();
    }

    @Transactional
    public int stockOf(UUID variantId) {
        return variantRepository.findById(variantId).orElseThrow().getStockQuantity();
    }

    @Transactional
    public void setVariantPrice(UUID variantId, BigDecimal price) {
        ProductVariant variant = variantRepository.findById(variantId).orElseThrow();
        variant.setPrice(price);
        variantRepository.save(variant);
    }
}