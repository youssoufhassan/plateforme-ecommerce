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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CartService {

    /** Garde-fou contre les commandes anormales, même en dropshipping. */
    private static final int MAX_QUANTITY_PER_ITEM = 20;

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
            throw new IllegalStateException("Cet article n'est plus disponible");
        }

        Optional<CartItem> existing = cart.getItems().stream()
                .filter(i -> i.getVariant() != null && i.getVariant().getId().equals(variant.getId()))
                .findFirst();

        int alreadyInCart = existing.map(CartItem::getQuantity).orElse(0);
        int requested = alreadyInCart + request.getQuantity();

        // Vérification du stock dès l'ajout, plutôt qu'au checkout
        checkQuantity(variant, requested, alreadyInCart);

        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQuantity(requested);
            item.setPriceAtAdd(variant.getPrice());
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

        ProductVariant variant = item.getVariant();
        if (variant == null || !variant.isAvailable()) {
            throw new IllegalStateException("Cet article n'est plus disponible");
        }

        checkQuantity(variant, quantity, 0);

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

    /** Vide le panier en une seule requête. */
    @Transactional
    public CartResponse clear(String userEmail) {
        Cart cart = getOrCreateCart(userEmail);
        cart.getItems().clear();
        cartRepository.save(cart);
        return toResponse(cart);
    }

    /**
     * Refuse une quantité impossible.
     * Pour un produit en stock propre, la limite est le stock réel ;
     * pour un produit dropshipping, seul le garde-fou général s'applique.
     */
    private void checkQuantity(ProductVariant variant, int requested, int alreadyInCart) {
        if (requested > MAX_QUANTITY_PER_ITEM) {
            throw new IllegalStateException(
                    "Quantité maximale de " + MAX_QUANTITY_PER_ITEM + " par article. Contactez-nous pour une commande plus importante.");
        }

        if (!"OWN_STOCK".equals(variant.getProduct().getFulfillmentType())) {
            return;
        }

        int stock = variant.getStockQuantity() != null ? variant.getStockQuantity() : 0;

        if (requested > stock) {
            if (alreadyInCart > 0) {
                throw new IllegalStateException(
                        "Il ne reste que " + stock + " exemplaire(s), dont " + alreadyInCart + " déjà dans votre panier");
            }
            throw new IllegalStateException("Il ne reste que " + stock + " exemplaire(s) disponible(s)");
        }
    }

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

    /**
     * Construit la réponse en revalidant chaque article :
     * disponibilité, stock restant et changement de prix depuis l'ajout.
     */
    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        boolean blocked = false;

        for (CartItem item : cart.getItems()) {
            ProductVariant variant = item.getVariant();
            String label = displayName(item);

            if (variant == null) {
                warnings.add(label + " n'est plus disponible, veuillez le retirer de votre panier");
                items.add(new CartItemResponse(item.getId(), item.getProduct().getId(), null,
                        item.getProduct().getName(), null, BigDecimal.ZERO, item.getQuantity(), false, 0));
                blocked = true;
                continue;
            }

            boolean available = variant.isAvailable();
            boolean ownStock = "OWN_STOCK".equals(variant.getProduct().getFulfillmentType());
            Integer maxQuantity = ownStock ? variant.getStockQuantity() : null;

            if (!available) {
                warnings.add(label + " n'est plus disponible, veuillez le retirer de votre panier");
                blocked = true;
            } else if (ownStock && item.getQuantity() > variant.getStockQuantity()) {
                warnings.add("Il ne reste que " + variant.getStockQuantity()
                        + " exemplaire(s) de " + label + ", veuillez réduire la quantité");
                blocked = true;
            }

            if (available && item.getPriceAtAdd() != null
                    && item.getPriceAtAdd().compareTo(variant.getPrice()) != 0) {
                warnings.add("Le prix de " + label + " a changé : "
                        + money(item.getPriceAtAdd()) + " → " + money(variant.getPrice()));
                // Le prix est mis à jour, mais la commande reste possible
                item.setPriceAtAdd(variant.getPrice());
                cartItemRepository.save(item);
            }

            items.add(new CartItemResponse(
                    item.getId(),
                    item.getProduct().getId(),
                    variant.getId(),
                    item.getProduct().getName(),
                    variant.getLabel(),
                    variant.getPrice(),
                    item.getQuantity(),
                    available,
                    maxQuantity));
        }

        // Seuls les articles achetables comptent dans le total
        BigDecimal subtotal = items.stream()
                .filter(CartItemResponse::isAvailable)
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
                pricing.amountUntilFreeShipping(),
                blocked,
                warnings);
    }

    private String displayName(CartItem item) {
        String name = item.getProduct().getName();
        ProductVariant variant = item.getVariant();

        if (variant == null || variant.getLabel() == null
                || "Standard".equalsIgnoreCase(variant.getLabel())) {
            return name;
        }
        return name + " — " + variant.getLabel();
    }

    private String money(BigDecimal amount) {
        return amount.setScale(2, java.math.RoundingMode.HALF_UP).toString().replace(".", ",") + " €";
    }
}