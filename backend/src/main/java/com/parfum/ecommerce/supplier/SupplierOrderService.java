package com.parfum.ecommerce.supplier;

import com.parfum.ecommerce.catalog.Product;
import com.parfum.ecommerce.order.Order;
import com.parfum.ecommerce.order.OrderItem;
import com.parfum.ecommerce.order.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class SupplierOrderService {

    private final SupplierOrderRepository supplierOrderRepository;
    private final OrderRepository orderRepository;

    public SupplierOrderService(SupplierOrderRepository supplierOrderRepository,
                                 OrderRepository orderRepository) {
        this.supplierOrderRepository = supplierOrderRepository;
        this.orderRepository = orderRepository;
    }

    /**
     * Génère les commandes fournisseur pour une commande client.
     * Un produit OWN_STOCK est ignoré : c'est nous qui l'expédions.
     * Les produits d'un même fournisseur sont regroupés dans une seule commande fournisseur.
     */
    @Transactional
    public List<SupplierOrder> generateForOrder(Order order) {
        Map<Supplier, List<OrderItem>> bySupplier = new LinkedHashMap<>();

        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();

            if (!"DROPSHIP".equals(product.getFulfillmentType())) continue;
            if (product.getSupplier() == null) continue;

            bySupplier.computeIfAbsent(product.getSupplier(), s -> new ArrayList<>()).add(item);
        }

        List<SupplierOrder> created = new ArrayList<>();

        for (Map.Entry<Supplier, List<OrderItem>> entry : bySupplier.entrySet()) {
            SupplierOrder supplierOrder = new SupplierOrder(order, entry.getKey());

            for (OrderItem item : entry.getValue()) {
                supplierOrder.getItems().add(
                        new SupplierOrderItem(supplierOrder, item)
                );
            }

            supplierOrderRepository.save(supplierOrder);
            created.add(supplierOrder);
        }

        return created;
    }

    public List<SupplierOrder> getPending() {
        return supplierOrderRepository.findByStatusOrderByCreatedAtAsc("TO_SEND");
    }

    public List<SupplierOrder> getAll() {
        return supplierOrderRepository.findAll();
    }

    public SupplierOrder markAsSent(UUID supplierOrderId, String externalReference) {
        SupplierOrder supplierOrder = supplierOrderRepository.findById(supplierOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Commande fournisseur introuvable"));

        if (!"TO_SEND".equals(supplierOrder.getStatus())) {
            throw new IllegalStateException("Cette commande fournisseur a déjà été traitée");
        }

        supplierOrder.setStatus("SENT");
        supplierOrder.setExternalReference(externalReference);
        supplierOrder.setSentAt(LocalDateTime.now());

        return supplierOrderRepository.save(supplierOrder);
    }

    public SupplierOrder updateStatus(UUID supplierOrderId, String newStatus) {
        if (!List.of("TO_SEND", "SENT", "CONFIRMED", "SHIPPED", "FAILED").contains(newStatus)) {
            throw new IllegalArgumentException("Statut invalide : " + newStatus);
        }

        SupplierOrder supplierOrder = supplierOrderRepository.findById(supplierOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Commande fournisseur introuvable"));

        supplierOrder.setStatus(newStatus);
        return supplierOrderRepository.save(supplierOrder);
    }
}