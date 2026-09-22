package com.parfum.ecommerce.order;

import com.parfum.ecommerce.cart.Cart;
import com.parfum.ecommerce.cart.CartItem;
import com.parfum.ecommerce.cart.CartRepository;
import com.parfum.ecommerce.catalog.Product;
import com.parfum.ecommerce.catalog.ProductRepository;
import com.parfum.ecommerce.catalog.ProductVariant;
import com.parfum.ecommerce.catalog.ProductVariantRepository;
import com.parfum.ecommerce.common.dto.PageResponse;
import com.parfum.ecommerce.identity.Address;
import com.parfum.ecommerce.identity.AddressRepository;
import com.parfum.ecommerce.identity.User;
import com.parfum.ecommerce.identity.UserRepository;
import com.parfum.ecommerce.legal.LegalPage;
import com.parfum.ecommerce.legal.LegalPageRepository;
import com.parfum.ecommerce.order.dto.OrderItemResponse;
import com.parfum.ecommerce.order.dto.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private static final List<String> VALID_STATUSES =
            List.of("PENDING", "PAID", "PREPARING", "SHIPPED", "DELIVERED", "CANCELLED");

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final PricingService pricingService;
    private final LegalPageRepository legalPageRepository;

    public OrderService(CartRepository cartRepository,
                         OrderRepository orderRepository,
                         ProductRepository productRepository,
                         ProductVariantRepository variantRepository,
                         UserRepository userRepository,
                         AddressRepository addressRepository,
                         PricingService pricingService,
                         LegalPageRepository legalPageRepository) {
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.pricingService = pricingService;
        this.legalPageRepository = legalPageRepository;
    }

    @Transactional
    public OrderResponse checkout(String userEmail, UUID addressId, boolean acceptTerms) {
        if (!acceptTerms) {
            throw new IllegalArgumentException("Vous devez accepter les conditions générales de vente");
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        if (!user.isEmailVerified()) {
            throw new IllegalStateException("Veuillez vérifier votre adresse email avant de commander");
        }

        if (addressId == null) {
            throw new IllegalArgumentException("Une adresse de livraison est obligatoire");
        }

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new IllegalArgumentException("Adresse introuvable"));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Cette adresse ne vous appartient pas");
        }

        Cart cart = cartRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new IllegalStateException("Panier vide"));

        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Impossible de commander un panier vide");
        }

        for (CartItem cartItem : cart.getItems()) {
            ProductVariant variant = cartItem.getVariant();

            if (variant == null) {
                throw new IllegalStateException("Votre panier contient un article obsolète, veuillez le retirer");
            }
            if (!variant.isActive()) {
                throw new IllegalStateException("Plus disponible : " + cartItem.getProduct().getName()
                        + " — " + variant.getLabel());
            }
            if ("OWN_STOCK".equals(variant.getProduct().getFulfillmentType())
                    && variant.getStockQuantity() < cartItem.getQuantity()) {
                throw new IllegalStateException("Stock insuffisant pour : " + cartItem.getProduct().getName()
                        + " — " + variant.getLabel());
            }
        }

        BigDecimal subtotal = cart.getItems().stream()
                .map(i -> i.getVariant().getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        PricingService.PriceBreakdown pricing =
                pricingService.calculate(subtotal, address.getCountryCode());

        Order order = new Order(user, pricing.total());
        order.setSubtotalAmount(pricing.subtotal());
        order.setShippingAmount(pricing.shipping());
        order.setVatAmount(pricing.vat());
        order.setVatRate(pricing.vatRate());
        order.setAddress(address);
        order.setExpiresAt(LocalDateTime.now().plusMinutes(30));

        // Preuve d'acceptation des CGV : date et version acceptée
        order.setTermsAcceptedAt(LocalDateTime.now());
        order.setTermsVersion(legalPageRepository.findBySlug("cgv")
                .map(LegalPage::getVersion)
                .orElse(1));

        for (CartItem cartItem : cart.getItems()) {
            order.getItems().add(new OrderItem(order, cartItem.getVariant(), cartItem.getQuantity()));
        }

        orderRepository.save(order);

        cart.getItems().clear();
        cartRepository.save(cart);

        return toResponse(order);
    }

    public List<OrderResponse> getMyOrders(String userEmail) {
        return orderRepository.findByUserEmailOrderByCreatedAtDesc(userEmail)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getOrdersPage(String status, int page, int size) {
        int safeSize = (size <= 0) ? 20 : Math.min(size, 50);
        PageRequest pageable = PageRequest.of(Math.max(0, page), safeSize,
                Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by("id")));

        Page<Order> result = (status == null || status.isBlank())
                ? orderRepository.findAll(pageable)
                : orderRepository.findByStatus(status.toUpperCase(), pageable);

        return PageResponse.of(result, this::toResponse);
    }

    @Transactional
    public OrderResponse updateStatus(UUID orderId, String newStatus) {
        if (!VALID_STATUSES.contains(newStatus)) {
            throw new IllegalArgumentException("Statut invalide : " + newStatus);
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Commande introuvable"));

        order.setStatus(newStatus);
        orderRepository.save(order);

        return toResponse(order);
    }

    /** Décrémente le stock de la variante (stock propre uniquement). Appelé après paiement. */
    @Transactional
    public void decrementStock(Order order) {
        for (OrderItem item : order.getItems()) {
            if (!"OWN_STOCK".equals(item.getProduct().getFulfillmentType())) continue;

            if (item.getVariant() != null) {
                ProductVariant variant = item.getVariant();
                variant.setStockQuantity(Math.max(0, variant.getStockQuantity() - item.getQuantity()));
                variantRepository.save(variant);
            } else {
                Product product = item.getProduct();
                product.setStockQuantity(Math.max(0, product.getStockQuantity() - item.getQuantity()));
                productRepository.save(product);
            }
        }
    }

    /** Remet le stock en cas d'annulation d'une commande déjà payée. */
    @Transactional
    public void restoreStock(Order order) {
        for (OrderItem item : order.getItems()) {
            if (!"OWN_STOCK".equals(item.getProduct().getFulfillmentType())) continue;

            if (item.getVariant() != null) {
                ProductVariant variant = item.getVariant();
                variant.setStockQuantity(variant.getStockQuantity() + item.getQuantity());
                variantRepository.save(variant);
            } else {
                Product product = item.getProduct();
                product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                productRepository.save(product);
            }
        }
    }

    @Transactional
    public OrderResponse cancelOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Commande introuvable"));

        if (List.of("SHIPPED", "DELIVERED", "CANCELLED").contains(order.getStatus())) {
            throw new IllegalStateException("Cette commande ne peut plus être annulée");
        }

        if (!"PENDING".equals(order.getStatus())) {
            restoreStock(order);
        }

        order.setStatus("CANCELLED");
        order.setExpiresAt(null);
        orderRepository.save(order);

        return toResponse(order);
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(i -> new OrderItemResponse(
                        i.getProduct().getName(),
                        i.getVariantLabel(),
                        i.getQuantity(),
                        i.getUnitPrice()))
                .toList();

        String customerFirstName = null;
        String customerLastName = null;
        String customerEmail = null;

        if (order.getUser() != null) {
            customerFirstName = order.getUser().getFirstName();
            customerLastName = order.getUser().getLastName();
            customerEmail = order.getUser().getEmail();
        }

        return new OrderResponse(
                order.getId(),
                order.getStatus(),
                order.getSubtotalAmount(),
                order.getShippingAmount(),
                order.getVatAmount(),
                order.getVatRate(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                customerFirstName,
                customerLastName,
                customerEmail,
                items
        );
    }
}