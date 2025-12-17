package com.vodafoneziggo.assignment.order.service;

import com.vodafoneziggo.assignment.order.contract.model.OrderResponse;
import com.vodafoneziggo.assignment.order.integration.ReqResClient;
import com.vodafoneziggo.assignment.order.model.OrderEntity;
import com.vodafoneziggo.assignment.order.repo.OrderRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    // Handles persistence and uniqueness checks
    private final OrderRepository repo;

    // Client for external user validation (ReqRes)
    private final ReqResClient reqResClient;

    public OrderService(OrderRepository repo, ReqResClient reqResClient) {
        this.repo = repo;
        this.reqResClient = reqResClient;
    }

    /**
     * Creates a new order for a given product and email.
     * Validates the user via external service and enforces uniqueness.
     */
    @Transactional
    public UUID createOrder(String productId, String email) {
        // Friendly pre-check (not sufficient under concurrency)
        if (repo.existsByEmailAndProductId(email, productId)) {
            throw new DuplicateOrderException();
        }

        // Verify user exists in external system
        var user = reqResClient.findUserByEmail(email)
                .orElseThrow(EmailNotFoundException::new);

        // Build new order entity
        OrderEntity e = new OrderEntity();
        e.setEmail(email);
        e.setProductId(productId);
        e.setFirstName(user.firstName());
        e.setLastName(user.lastName());

        try {
            // Save and return generated order id
            return repo.save(e).getOrderId();
        } catch (DataIntegrityViolationException ex) {
            // Real protection (DB unique constraint)
            throw new DuplicateOrderException();
        }
    }

    /**
     * Returns raw order entities for a given email.
     */
    public List<OrderEntity> getOrdersByEmail(String email) {
        return repo.findAllByEmailIgnoreCase(email);
    }

    // Thrown when the same customer orders the same product twice
    public static class DuplicateOrderException extends RuntimeException {}

    // Thrown when email does not exist in external user system
    public static class EmailNotFoundException extends RuntimeException {}

    // Reserved for wrapping external service failures if needed
    public static class ExternalServiceException extends RuntimeException {
        public ExternalServiceException(Throwable cause) {
            super(cause);
        }
    }

    /**
     * Maps order entities to API response objects.
     */
    public List<OrderResponse> getOrderResponsesByEmail(String email) {
        return repo.findAllByEmailIgnoreCase(email).stream()
                .map(e -> new OrderResponse(
                        e.getOrderId(),
                        e.getEmail(),
                        e.getFirstName(),
                        e.getLastName(),
                        e.getProductId()
                ))
                .collect(Collectors.toList());
    }
}
