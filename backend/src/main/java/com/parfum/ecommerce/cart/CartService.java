package com.parfum.ecommerce.cart;

import com.parfum.ecommerce.cart.dto.AddItemRequest;
import com.parfum.ecommerce.cart.dto.CartItemResponse;
import com.parfum.ecommerce.cart.dto.CartResponse;
import com.parfum.ecommerce.catalog.Product;
import com.parfum.ecommerce.catalog.ProductRepository;
import com.parfum.ecommerce.catalog.ProductVariant;
import com.parfum.ecommerce.identity.User;
import com.parfum.ecommerce.identity.UserRepository;
import com.parfum.ecommerce.order.PricingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PricingService pricingService;

    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository,
                        ProductRepository productRepository, UserRepository userRepository,
                        PricingService pricingService) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.pricingService = pricingService;
    }

    @Transactional
    public CartResponse getCart(String userEmail) {
        return toResponse(getOrCreateCart(userEmail));
    }

    @Transactional
    public CartResponse addItem(String userEmail, AddItemRequest request) {
        Cart cart = getOrCreateCart(userEmail);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Produit introuvable"));

        ProductVariant variant = resolveVariant(product, request.getVariantId());

        if (!variant.isAvailable()) {
            throw new IllegalStateException("Cette variante n'est plus disponible");
        }

        // Même variante déjà dans le panier : on augmente la quantité au lieu d'ajouter une ligne
        Optional<CartItem> existing = cart.getItems().stream()
                .filter(i -> i.getVariant() != null && i.getVariant().getId().equals(variant.getId()))
                .findFirst();

        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            cartItemRepository.save(item);
        } else {
            CartItem item = new CartItem(cart, variant, request.getQuantity());
            cart.getItems().add(item);
            cartItemRepository.save(item);
        }

        return toResponse(cart);
    }

    @Transactional
    public CartResponse updateItemQuantity(String userEmail, UUID itemId, Integer quantity) {
        if (quantity == null || quantity < 1) {
            throw new IllegalArgumentException("La quantité doit être au moins 1");
        }

        Cart cart = getOrCreateCart(userEmail);

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Article introuvable dans votre panier"));

        item.setQuantity(quantity);
        cartItemRepository.save(item);

        return toResponse(cart);
    }

    @Transactional
    public CartResponse removeItem(String userEmail, UUID itemId) {
        Cart cart = getOrCreateCart(userEmail);
        cart.getItems().removeIf(item -> item.getId().equals(itemId));
        cartRepository.save(cart);

        return toResponse(cart);
    }

    /**
     * Choix de la variante : celle demandée, ou la seule variante active
     * si le produit n'en a qu'une (compatibilité avec le frontend actuel).
     */
    private ProductVariant resolveVariant(Product product, UUID variantId) {
        List<ProductVariant> active = product.getActiveVariants();

        if (variantId != null) {
            return active.stream()
                    .filter(v -> v.getId().equals(variantId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Variante introuvable pour ce produit"));
        }

        if (active.size() == 1) {
            return active.get(0);
        }

        if (active.isEmpty()) {
            throw new IllegalStateException("Ce produit n'est pas disponible");
        }

        throw new IllegalArgumentException("Veuillez choisir une variante (taille, format...)");
    }

    private Cart getOrCreateCart(String userEmail) {
        return cartRepository.findByUserEmail(userEmail)
                .orElseGet(() -> {
                    User user = userRepository.findByEmail(userEmail)
                            .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
                    return cartRepository.save(new Cart(user));
                });
    }

    private BigDecimal unitPriceOf(CartItem item) {
        return item.getVariant() != null ? item.getVariant().getPrice() : item.getProduct().getPrice();
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(item -> new CartItemResponse(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getVariant() != null ? item.getVariant().getId() : null,
                        item.getProduct().getName(),
                        item.getVariant() != null ? item.getVariant().getLabel() : null,
                        unitPriceOf(item),
                        item.getQuantity()))
                .toList();

        BigDecimal subtotal = items.stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Estimation basée sur la France : le pays réel est connu au checkout
        PricingService.PriceBreakdown pricing = pricingService.calculate(subtotal, "FR");

        return new CartResponse(
                items,
                pricing.subtotal(),
                pricing.shipping(),
                pricing.vat(),
                pricing.vatRate(),
                pricing.total(),
                pricing.freeShippingThreshold(),
                pricing.amountUntilFreeShipping()
        );
    }
}