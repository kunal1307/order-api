package com.vodafoneziggo.assignment.order.repo;

import com.vodafoneziggo.assignment.order.model.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * JPA repository for OrderEntity.
 * Handles basic CRUD and a few domain-specific lookups.
 */
public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {
    // Used to enforce "one product per customer" rule
    boolean existsByEmailAndProductId(String email, String productId);

    // Fetch all orders for a customer, email comparison is case-insensitive
    List<OrderEntity> findAllByEmailIgnoreCase(String email);
}
