package com.vodafoneziggo.assignment.order.controller;

import com.vodafoneziggo.assignment.order.contract.api.OrdersApi;
import com.vodafoneziggo.assignment.order.contract.model.CreateOrderRequest;
import com.vodafoneziggo.assignment.order.contract.model.CreateOrderResponse;
import com.vodafoneziggo.assignment.order.contract.model.OrderResponse;
import com.vodafoneziggo.assignment.order.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class OrderController implements OrdersApi {

    // Business logic lives in the service; controller just adapts HTTP <-> service
    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<CreateOrderResponse> apiOrdersPost(CreateOrderRequest request) {
        // Delegate to service for validation + persistence
        UUID orderId = service.createOrder(request.getProductId(), request.getEmail());

        // Contract-first: respond using generated DTO
        CreateOrderResponse resp = new CreateOrderResponse();
        resp.setOrderId(orderId);

        return ResponseEntity.status(201).body(resp);
    }

    @Override
    public ResponseEntity<List<OrderResponse>> apiOrdersGet(String email) {
        // Fetch entities and map them to contract DTOs for the API response
        return ResponseEntity.ok(
                service.getOrdersByEmail(email).stream()
                        .map(e -> {
                            OrderResponse r = new OrderResponse();
                            r.setOrderId(e.getOrderId());
                            r.setEmail(e.getEmail());
                            r.setFirstName(e.getFirstName());
                            r.setLastName(e.getLastName());
                            r.setProductId(e.getProductId());
                            return r;
                        })
                        .toList()
        );
    }
}

