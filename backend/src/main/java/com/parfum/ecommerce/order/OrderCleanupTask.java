package com.parfum.ecommerce.order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class OrderCleanupTask {

    private static final Logger log = LoggerFactory.getLogger(OrderCleanupTask.class);

    private final OrderRepository orderRepository;

    public OrderCleanupTask(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /** Toutes les 10 minutes : annule les commandes non payées expirées. */
    @Scheduled(fixedRate = 600_000)
    @Transactional
    public void cancelExpiredOrders() {
        List<Order> expired = orderRepository.findByStatusAndExpiresAtBefore("PENDING", LocalDateTime.now());

        for (Order order : expired) {
            order.setStatus("CANCELLED");
            order.setExpiresAt(null);
            orderRepository.save(order);
        }

        if (!expired.isEmpty()) {
            log.info("{} commande(s) expirée(s) annulée(s)", expired.size());
        }
    }
}