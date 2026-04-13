package com.bookstore.order.repository;

import com.bookstore.order.model.Cart;
import com.google.cloud.firestore.DocumentSnapshot;
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
public class CartRepository {

    private static final String COLLECTION = "carts";

    public Cart getCart(String userId) {
        try {
            DocumentSnapshot doc = FirestoreClient.getFirestore()
                .collection(COLLECTION).document(userId).get().get();
            if (!doc.exists() || doc.getData() == null) {
                return Cart.builder().userId(userId).items(new ArrayList<>()).build();
            }
            return fromMap(userId, doc.getData());
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to get cart", e);
        }
    }

    public Cart saveCart(Cart cart) {
        try {
            cart.setUpdatedAt(Instant.now());
            FirestoreClient.getFirestore()
                .collection(COLLECTION).document(cart.getUserId())
                .set(toMap(cart)).get();
            return cart;
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to save cart", e);
        }
    }

    public void clearCart(String userId) {
        try {
            FirestoreClient.getFirestore()
                .collection(COLLECTION).document(userId).delete().get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to clear cart", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toMap(Cart cart) {
        List<Map<String, Object>> items = cart.getItems().stream().map(i -> {
            Map<String, Object> m = new HashMap<>();
            m.put("bookId",       i.getBookId());
            m.put("title",        i.getTitle());
            m.put("author",       i.getAuthor());
            m.put("coverImageUrl",i.getCoverImageUrl());
            m.put("quantity",     i.getQuantity());
            m.put("unitPrice",    i.getUnitPrice().doubleValue());
            m.put("sellerId",     i.getSellerId());
            return m;
        }).collect(Collectors.toList());

        return Map.of("userId", cart.getUserId(), "items", items,
                      "updatedAt", Instant.now().toEpochMilli());
    }

    @SuppressWarnings("unchecked")
    private Cart fromMap(String userId, Map<String, Object> data) {
        List<Map<String, Object>> rawItems = (List<Map<String, Object>>) data.getOrDefault("items", List.of());
        List<Cart.CartItem> items = rawItems.stream().map(m -> Cart.CartItem.builder()
            .bookId((String) m.get("bookId"))
            .title((String) m.get("title"))
            .author((String) m.get("author"))
            .coverImageUrl((String) m.get("coverImageUrl"))
            .quantity(((Number) m.get("quantity")).intValue())
            .unitPrice(BigDecimal.valueOf(((Number) m.get("unitPrice")).doubleValue()))
            .sellerId((String) m.get("sellerId"))
            .build()
        ).collect(Collectors.toList());

        return Cart.builder().userId(userId).items(items)
            .updatedAt(data.get("updatedAt") != null
                ? Instant.ofEpochMilli(((Number) data.get("updatedAt")).longValue()) : null)
            .build();
    }
}
