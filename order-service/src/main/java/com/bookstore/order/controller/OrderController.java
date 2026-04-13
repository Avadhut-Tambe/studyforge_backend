package com.bookstore.order.controller;

import com.bookstore.order.dto.CheckoutRequest;
import com.bookstore.order.model.Order;
import com.bookstore.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout")
    public ResponseEntity<Order> checkout(
        @RequestHeader("X-User-Id") String userId,
        @Valid @RequestBody CheckoutRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(orderService.createOrderFromCart(userId, request));
    }

    @GetMapping
    public ResponseEntity<List<Order>> getMyOrders(@RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(orderService.getUserOrders(userId));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrder(
        @PathVariable String orderId,
        @RequestHeader("X-User-Id")   String userId,
        @RequestHeader("X-User-Role") String role
    ) {
        return ResponseEntity.ok(orderService.getOrder(orderId, userId, role));
    }

    // Called internally by payment-service (or admin) to update order status
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<Order> updateStatus(
        @PathVariable String orderId,
        @RequestBody java.util.Map<String, String> body
    ) {
        Order.OrderStatus status = Order.OrderStatus.valueOf(body.get("status"));
        String paymentId = body.get("paymentId");
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, status, paymentId));
    }
}
