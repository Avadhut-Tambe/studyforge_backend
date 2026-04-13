package com.bookstore.order.repository;

import com.bookstore.order.model.Order;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class OrderRepository {

    private static final String COLLECTION = "orders";

    private CollectionReference col() {
        return FirestoreClient.getFirestore().collection(COLLECTION);
    }

    public Order save(Order order) {
        try {
            Map<String, Object> data = toMap(order);
            if (order.getId() == null) {
                DocumentReference ref = col().add(data).get();
                order.setId(ref.getId());
                ref.update("id", ref.getId());
            } else {
                col().document(order.getId()).set(data).get();
            }
            return order;
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to save order", e);
        }
    }

    public Optional<Order> findById(String id) {
        try {
            DocumentSnapshot doc = col().document(id).get().get();
            if (!doc.exists()) return Optional.empty();
            return Optional.of(fromMap(doc.getId(), doc.getData()));
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to find order", e);
        }
    }

    public List<Order> findByUserId(String userId) {
        try {
            QuerySnapshot snap = col().whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get().get();
            return snap.getDocuments().stream()
                .map(d -> fromMap(d.getId(), d.getData()))
                .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to find orders", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toMap(Order o) {
        Map<String, Object> m = new HashMap<>();
        m.put("userId",        o.getUserId());
        m.put("subtotal",      o.getSubtotal()      != null ? o.getSubtotal().doubleValue()      : 0.0);
        m.put("shippingCharge",o.getShippingCharge()!= null ? o.getShippingCharge().doubleValue(): 0.0);
        m.put("total",         o.getTotal()         != null ? o.getTotal().doubleValue()         : 0.0);
        m.put("status",        o.getStatus().name());
        m.put("paymentId",     o.getPaymentId());
        m.put("paymentMethod", o.getPaymentMethod());
        m.put("trackingId",    o.getTrackingId());
        m.put("createdAt",     o.getCreatedAt() != null ? o.getCreatedAt().toEpochMilli() : Instant.now().toEpochMilli());
        m.put("updatedAt",     Instant.now().toEpochMilli());

        if (o.getShippingAddress() != null) {
            Order.Address a = o.getShippingAddress();
            m.put("shippingAddress", Map.of(
                "name", nvl(a.getName()), "street", nvl(a.getStreet()),
                "city", nvl(a.getCity()), "state",  nvl(a.getState()),
                "pincode", nvl(a.getPincode()), "phone", nvl(a.getPhone())
            ));
        }

        if (o.getItems() != null) {
            List<Map<String, Object>> items = o.getItems().stream().map(i -> {
                Map<String, Object> im = new HashMap<>();
                im.put("bookId",       i.getBookId());
                im.put("title",        i.getTitle());
                im.put("author",       i.getAuthor());
                im.put("coverImageUrl",i.getCoverImageUrl());
                im.put("quantity",     i.getQuantity());
                im.put("unitPrice",    i.getUnitPrice().doubleValue());
                im.put("totalPrice",   i.getTotalPrice().doubleValue());
                im.put("sellerId",     i.getSellerId());
                return im;
            }).collect(Collectors.toList());
            m.put("items", items);
        }
        return m;
    }

    @SuppressWarnings("unchecked")
    private Order fromMap(String id, Map<String, Object> m) {
        List<Map<String, Object>> rawItems = (List<Map<String, Object>>) m.getOrDefault("items", List.of());
        List<Order.OrderItem> items = rawItems.stream().map(i -> Order.OrderItem.builder()
            .bookId((String) i.get("bookId"))
            .title((String) i.get("title"))
            .author((String) i.get("author"))
            .coverImageUrl((String) i.get("coverImageUrl"))
            .quantity(((Number) i.get("quantity")).intValue())
            .unitPrice(BigDecimal.valueOf(((Number) i.get("unitPrice")).doubleValue()))
            .totalPrice(BigDecimal.valueOf(((Number) i.get("totalPrice")).doubleValue()))
            .sellerId((String) i.get("sellerId"))
            .build()
        ).collect(Collectors.toList());

        Order.Address addr = null;
        if (m.get("shippingAddress") instanceof Map) {
            Map<String, Object> a = (Map<String, Object>) m.get("shippingAddress");
            addr = Order.Address.builder()
                .name((String) a.get("name")).street((String) a.get("street"))
                .city((String) a.get("city")).state((String) a.get("state"))
                .pincode((String) a.get("pincode")).phone((String) a.get("phone"))
                .build();
        }

        return Order.builder()
            .id(id).userId((String) m.get("userId"))
            .items(items)
            .subtotal(BigDecimal.valueOf(((Number) m.getOrDefault("subtotal", 0.0)).doubleValue()))
            .shippingCharge(BigDecimal.valueOf(((Number) m.getOrDefault("shippingCharge", 0.0)).doubleValue()))
            .total(BigDecimal.valueOf(((Number) m.getOrDefault("total", 0.0)).doubleValue()))
            .status(Order.OrderStatus.valueOf((String) m.get("status")))
            .paymentId((String) m.get("paymentId"))
            .paymentMethod((String) m.get("paymentMethod"))
            .trackingId((String) m.get("trackingId"))
            .shippingAddress(addr)
            .createdAt(m.get("createdAt") != null ? Instant.ofEpochMilli(((Number) m.get("createdAt")).longValue()) : null)
            .updatedAt(m.get("updatedAt") != null ? Instant.ofEpochMilli(((Number) m.get("updatedAt")).longValue()) : null)
            .build();
    }

    private String nvl(String s) { return s != null ? s : ""; }
}
