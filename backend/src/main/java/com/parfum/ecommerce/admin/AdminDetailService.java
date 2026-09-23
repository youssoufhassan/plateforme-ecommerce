package com.parfum.ecommerce.admin;

import com.parfum.ecommerce.admin.dto.CustomerDetailResponse;
import com.parfum.ecommerce.admin.dto.OrderAdminDetailResponse;
import com.parfum.ecommerce.identity.Address;
import com.parfum.ecommerce.identity.AddressRepository;
import com.parfum.ecommerce.identity.User;
import com.parfum.ecommerce.identity.UserRepository;
import com.parfum.ecommerce.invoice.InvoiceRepository;
import com.parfum.ecommerce.order.Order;
import com.parfum.ecommerce.order.OrderRepository;
import com.parfum.ecommerce.order.OrderStatusHistoryRepository;
import com.parfum.ecommerce.payment.PaymentRepository;
import com.parfum.ecommerce.shipping.ShipmentRepository;
import com.parfum.ecommerce.supplier.SupplierOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AdminDetailService {

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository statusHistoryRepository;
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final ShipmentRepository shipmentRepository;
    private final SupplierOrderRepository supplierOrderRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    public AdminDetailService(OrderRepository orderRepository,
                               OrderStatusHistoryRepository statusHistoryRepository,
                               PaymentRepository paymentRepository,
                               InvoiceRepository invoiceRepository,
                               ShipmentRepository shipmentRepository,
                               SupplierOrderRepository supplierOrderRepository,
                               UserRepository userRepository,
                               AddressRepository addressRepository) {
        this.orderRepository = orderRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
        this.shipmentRepository = shipmentRepository;
        this.supplierOrderRepository = supplierOrderRepository;
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
    }

    /** Tout ce qui concerne une commande, en une seule réponse. */
    @Transactional(readOnly = true)
    public OrderAdminDetailResponse orderDetail(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Commande introuvable"));

        List<OrderAdminDetailResponse.Item> items = order.getItems().stream()
                .map(item -> new OrderAdminDetailResponse.Item(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getVariantLabel(),
                        item.getProduct().getImageUrl(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                                .setScale(2, RoundingMode.HALF_UP),
                        item.getProduct().getFulfillmentType(),
                        item.getProduct().getSupplier() != null
                                ? item.getProduct().getSupplier().getName() : null,
                        item.getVariant() != null ? item.getVariant().getSupplierSku()
                                : item.getProduct().getSupplierSku()
                ))
                .toList();

        var payment = paymentRepository.findByOrderIdOrderByIdAsc(orderId).stream()
                .filter(p -> "SUCCESS".equals(p.getStatus()))
                .findFirst()
                .or(() -> paymentRepository.findByOrderIdOrderByIdAsc(orderId).stream()
                        .reduce((first, second) -> second))
                .map(p -> new OrderAdminDetailResponse.Payment(
                        p.getProvider(), p.getStatus(), p.getTransactionReference(), p.getPaidAt()))
                .orElse(null);

        var invoice = invoiceRepository.findByOrderId(orderId)
                .map(i -> new OrderAdminDetailResponse.Invoice(
                        i.getId(), i.getInvoiceNumber(), i.getIssuedAt()))
                .orElse(null);

        var shipment = shipmentRepository.findByOrderId(orderId)
                .map(s -> new OrderAdminDetailResponse.Shipment(
                        s.getCarrier(), s.getTrackingNumber(), s.getShippedAt()))
                .orElse(null);

        List<OrderAdminDetailResponse.SupplierOrder> supplierOrders =
                supplierOrderRepository.findByOrderId(orderId).stream()
                        .map(so -> new OrderAdminDetailResponse.SupplierOrder(
                                so.getId(),
                                so.getSupplier().getName(),
                                so.getStatus(),
                                so.getExternalReference(),
                                so.getItems().stream()
                                        .filter(i -> i.getUnitCost() != null)
                                        .map(i -> i.getUnitCost().multiply(BigDecimal.valueOf(i.getQuantity())))
                                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                                        .setScale(2, RoundingMode.HALF_UP)))
                        .toList();

        List<OrderAdminDetailResponse.StatusChange> history =
                statusHistoryRepository.findByOrderIdOrderByCreatedAtAsc(orderId).stream()
                        .map(h -> new OrderAdminDetailResponse.StatusChange(
                                h.getPreviousStatus(), h.getStatus(), h.getChangedBy(),
                                h.getNote(), h.getCreatedAt()))
                        .toList();

        return new OrderAdminDetailResponse(
                order.getId(),
                shortId(order.getId()),
                order.getStatus(),
                order.getCreatedAt(),
                customerOf(order),
                addressOf(order.getAddress()),
                items,
                new OrderAdminDetailResponse.Amounts(
                        order.getSubtotalAmount(), order.getShippingAmount(),
                        order.getVatAmount(), order.getVatRate(), order.getTotalAmount()),
                payment,
                invoice,
                shipment,
                supplierOrders,
                new OrderAdminDetailResponse.Terms(order.getTermsAcceptedAt(), order.getTermsVersion()),
                history,
                items.stream().anyMatch(i -> "DROPSHIP".equals(i.fulfillmentType())),
                items.stream().anyMatch(i -> "OWN_STOCK".equals(i.fulfillmentType()))
        );
    }

    /** Fiche client : coordonnées, statistiques, adresses et historique de commandes. */
    @Transactional(readOnly = true)
    public CustomerDetailResponse customerDetail(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Client introuvable"));

        Object[] raw = orderRepository.customerStats(userId);
        Object[] stats = (raw != null && raw.length == 1 && raw[0] instanceof Object[] inner) ? inner : raw;

        long orderCount = stats != null && stats[0] != null ? ((Number) stats[0]).longValue() : 0;
        BigDecimal totalSpent = stats != null && stats[1] != null
                ? ((BigDecimal) stats[1]).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        LocalDateTime lastOrderAt = stats != null && stats[2] != null ? (LocalDateTime) stats[2] : null;

        BigDecimal averageBasket = orderCount > 0
                ? totalSpent.divide(BigDecimal.valueOf(orderCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        List<CustomerDetailResponse.Address> addresses =
                addressRepository.findByUserEmail(user.getEmail()).stream()
                        .map(a -> new CustomerDetailResponse.Address(
                                a.getFirstName(), a.getLastName(), a.getStreet(), a.getComplement(),
                                a.getPostalCode(), a.getCity(), a.getCountryCode(), a.getPhone()))
                        .toList();

        List<CustomerDetailResponse.OrderSummary> orders =
                orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                        .map(o -> new CustomerDetailResponse.OrderSummary(
                                o.getId(), shortId(o.getId()), o.getCreatedAt(),
                                o.getTotalAmount(), o.getStatus()))
                        .toList();

        return new CustomerDetailResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                user.isGuest(),
                user.isEmailVerified(),
                user.isMarketingConsent(),
                user.getCreatedAt(),
                new CustomerDetailResponse.Stats(orderCount, totalSpent, averageBasket, lastOrderAt),
                addresses,
                orders
        );
    }

    private OrderAdminDetailResponse.Customer customerOf(Order order) {
        User user = order.getUser();
        if (user == null) {
            return new OrderAdminDetailResponse.Customer(null, "Client", null, null, false);
        }

        Address address = order.getAddress();
        String name = (address != null && address.getFirstName() != null)
                ? (address.getFirstName() + " " + (address.getLastName() != null ? address.getLastName() : "")).trim()
                : ((user.getFirstName() != null ? user.getFirstName() : "") + " "
                    + (user.getLastName() != null ? user.getLastName() : "")).trim();

        return new OrderAdminDetailResponse.Customer(
                user.getId(),
                name.isEmpty() ? user.getEmail() : name,
                user.getEmail(),
                user.getPhone(),
                user.isGuest());
    }

    private OrderAdminDetailResponse.ShippingAddress addressOf(Address a) {
        if (a == null) return null;
        return new OrderAdminDetailResponse.ShippingAddress(
                a.getFirstName(), a.getLastName(), a.getStreet(), a.getComplement(),
                a.getPostalCode(), a.getCity(), a.getCountryCode(), a.getPhone());
    }

    private String shortId(UUID id) {
        return id.toString().substring(0, 8).toUpperCase();
    }
}