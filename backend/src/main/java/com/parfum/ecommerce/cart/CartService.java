package com.parfum.ecommerce.cart;

import com.parfum.ecommerce.cart.dto.AddItemRequest;
import com.parfum.ecommerce.cart.dto.CartItemResponse;
import com.parfum.ecommerce.cart.dto.CartResponse;
import com.parfum.ecommerce.catalog.Product;
import com.parfum.ecommerce.catalog.ProductRepository;
import com.parfum.ecommerce.identity.User;
import com.parfum.ecommerce.identity.UserRepository;
import com.parfum.ecommerce.order.PricingService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
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

    public CartResponse getCart(String userEmail) {
        return toResponse(getOrCreateCart(userEmail));
    }

    public CartResponse addItem(String userEmail, AddItemRequest request) {
        Cart cart = getOrCreateCart(userEmail);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Produit introuvable"));

        CartItem item = new CartItem(cart, product, request.getQuantity());
        cart.getItems().add(item);
        cartItemRepository.save(item);

        return toResponse(cart);
    }

    public CartResponse updateItemQuantity(String userEmail, UUID itemId, Integer quantity) {
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Article introuvable"));

        item.setQuantity(quantity);
        cartItemRepository.save(item);

        return getCart(userEmail);
    }

    public CartResponse removeItem(String userEmail, UUID itemId) {
        Cart cart = getOrCreateCart(userEmail);
        cart.getItems().removeIf(item -> item.getId().equals(itemId));
        cartRepository.save(cart);

        return toResponse(cart);
    }

    private Cart getOrCreateCart(String userEmail) {
        return cartRepository.findByUserEmail(userEmail)
                .orElseGet(() -> {
                    User user = userRepository.findByEmail(userEmail)
                            .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
                    return cartRepository.save(new Cart(user));
                });
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(item -> new CartItemResponse(
                        item.getId(),
                        item.getProduct().getName(),
                        item.getProduct().getPrice(),
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