package com.vodafoneziggo.assignment.order.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * JPA entity representing a persisted order.
 */
@Getter
@Setter
@Entity
@Table(
        name = "orders",
        // Prevents the same customer from ordering the same product twice
        uniqueConstraints = @UniqueConstraint(
                name = "uq_orders_email_product",
                columnNames = {"email", "product_id"}
        )
)
public class OrderEntity {

    // Primary key for the order
    @Id
    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    // Customer email used to identify the order owner
    @Column(name = "email", nullable = false, length = 320)
    private String email;

    // First name fetched from external user service
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    // Last name fetched from external user service
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    // Product identifier being ordered
    @Column(name = "product_id", nullable = false, length = 100)
    private String productId;

    // Timestamp when the order was created
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    /**
     * Initializes technical fields before persisting the entity.
     */
    @PrePersist
    void prePersist() {
        if (orderId == null) orderId = UUID.randomUUID();
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }
}
