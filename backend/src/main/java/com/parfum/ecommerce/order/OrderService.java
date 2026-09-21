package com.parfum.ecommerce.order;

import com.parfum.ecommerce.cart.Cart;
import com.parfum.ecommerce.cart.CartItem;
import com.parfum.ecommerce.cart.CartRepository;
import com.parfum.ecommerce.catalog.Product;
import com.parfum.ecommerce.catalog.ProductRepository;
import com.parfum.ecommerce.identity.Address;
import com.parfum.ecommerce.identity.AddressRepository;
import com.parfum.ecommerce.identity.User;
import com.parfum.ecommerce.identity.UserRepository;
import com.parfum.ecommerce.order.dto.OrderItemResponse;
import com.parfum.ecommerce.order.dto.OrderResponse;
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
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final PricingService pricingService;

    public OrderService(CartRepository cartRepository,
                         OrderRepository orderRepository,
                         ProductRepository productRepository,
                         UserRepository userRepository,
                         AddressRepository addressRepository,
                         PricingService pricingService) {
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.pricingService = pricingService;
    }

    @Transactional
    public OrderResponse checkout(String userEmail, UUID addressId) {
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
            Product product = cartItem.getProduct();
            if ("OWN_STOCK".equals(product.getFulfillmentType())
                    && product.getStockQuantity() < cartItem.getQuantity()) {
                throw new IllegalStateException("Stock insuffisant pour : " + product.getName());
            }
        }

        BigDecimal subtotal = cart.getItems().stream()
                .map(i -> i.getProduct().getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
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

        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            order.getItems().add(
                    new OrderItem(order, product, cartItem.getQuantity(), product.getPrice()));
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

    /** Décrémente le stock des produits en stock propre. Appelé après paiement confirmé. */
    @Transactional
    public void decrementStock(Order order) {
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            if ("OWN_STOCK".equals(product.getFulfillmentType())) {
                int newStock = product.getStockQuantity() - item.getQuantity();
                product.setStockQuantity(Math.max(0, newStock));
                productRepository.save(product);
            }
        }
    }

    /** Remet le stock en cas d'annulation d'une commande déjà payée. */
    @Transactional
    public void restoreStock(Order order) {
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            if ("OWN_STOCK".equals(product.getFulfillmentType())) {
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
                        i.getProduct().getName(), i.getQuantity(), i.getUnitPrice()))
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