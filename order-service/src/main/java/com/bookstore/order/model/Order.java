package com.bookstore.order.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    private String      id;
    private String      userId;
    private List<OrderItem> items;
    private BigDecimal  subtotal;
    private BigDecimal  shippingCharge;
    private BigDecimal  total;
    private OrderStatus status;
    private String      paymentId;
    private String      paymentMethod;
    private Address     shippingAddress;
    private Instant     createdAt;
    private Instant     updatedAt;
    private String      trackingId;

    public enum OrderStatus {
        PENDING,
        PAYMENT_PROCESSING,
        PAYMENT_FAILED,
        CONFIRMED,
        PROCESSING,
        SHIPPED,
        DELIVERED,
        CANCELLED,
        REFUNDED
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem {
        private String     bookId;
        private String     title;
        private String     author;
        private String     coverImageUrl;
        private int        quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        private String     sellerId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Address {
        private String name;
        private String street;
        private String city;
        private String state;
        private String pincode;
        private String phone;
    }
}
