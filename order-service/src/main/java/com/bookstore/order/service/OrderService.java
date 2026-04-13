package com.bookstore.order.service;

import com.bookstore.order.dto.CheckoutRequest;
import com.bookstore.order.model.Cart;
import com.bookstore.order.model.Order;
import com.bookstore.order.repository.CartRepository;
import com.bookstore.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository  cartRepository;

    private static final BigDecimal SHIPPING_CHARGE = new BigDecimal("49.00");
    private static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("500.00");

    /**
     * Convert the user's cart into an order (PENDING state).
     * Payment service will update status to CONFIRMED after successful payment.
     */
    public Order createOrderFromCart(String userId, CheckoutRequest request) {
        Cart cart = cartRepository.getCart(userId);

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        List<Order.OrderItem> orderItems = cart.getItems().stream()
            .map(i -> Order.OrderItem.builder()
                .bookId(i.getBookId())
                .title(i.getTitle())
                .author(i.getAuthor())
                .coverImageUrl(i.getCoverImageUrl())
                .quantity(i.getQuantity())
                .unitPrice(i.getUnitPrice())
                .totalPrice(i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .sellerId(i.getSellerId())
                .build())
            .collect(Collectors.toList());

        BigDecimal subtotal = cart.getTotal();
        BigDecimal shipping = subtotal.compareTo(FREE_SHIPPING_THRESHOLD) >= 0
            ? BigDecimal.ZERO : SHIPPING_CHARGE;

        Order order = Order.builder()
            .userId(userId)
            .items(orderItems)
            .subtotal(subtotal)
            .shippingCharge(shipping)
            .total(subtotal.add(shipping))
            .status(Order.OrderStatus.PENDING)
            .paymentMethod(request.getPaymentMethod())
            .shippingAddress(request.getShippingAddress())
            .trackingId(generateTrackingId())
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        Order saved = orderRepository.save(order);
        log.info("Order created: {} for user: {}", saved.getId(), userId);
        return saved;
    }

    public Order getOrder(String orderId, String userId, String role) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        if (!"ADMIN".equals(role) && !order.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }
        return order;
    }

    public List<Order> getUserOrders(String userId) {
        return orderRepository.findByUserId(userId);
    }

    public Order updateOrderStatus(String orderId, Order.OrderStatus newStatus, String paymentId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(newStatus);
        order.setUpdatedAt(Instant.now());
        if (paymentId != null) order.setPaymentId(paymentId);
        return orderRepository.save(order);
    }

    private String generateTrackingId() {
        return "BS" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }
}
