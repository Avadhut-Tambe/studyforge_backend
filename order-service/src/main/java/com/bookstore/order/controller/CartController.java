package com.bookstore.order.controller;

import com.bookstore.order.model.Cart;
import com.bookstore.order.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<Cart> getCart(@RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @PostMapping("/items")
    public ResponseEntity<Cart> addItem(
        @RequestHeader("X-User-Id") String userId,
        @RequestBody Map<String, Object> body
    ) {
        Cart.CartItem item = Cart.CartItem.builder()
            .bookId((String) body.get("bookId"))
            .title((String) body.get("title"))
            .author((String) body.get("author"))
            .coverImageUrl((String) body.get("coverImageUrl"))
            .quantity(((Number) body.getOrDefault("quantity", 1)).intValue())
            .unitPrice(BigDecimal.valueOf(((Number) body.get("unitPrice")).doubleValue()))
            .sellerId((String) body.get("sellerId"))
            .build();
        return ResponseEntity.ok(cartService.addItem(userId, item));
    }

    @PatchMapping("/items/{bookId}")
    public ResponseEntity<Cart> updateQuantity(
        @RequestHeader("X-User-Id") String userId,
        @PathVariable String bookId,
        @RequestBody Map<String, Integer> body
    ) {
        return ResponseEntity.ok(cartService.updateItemQuantity(userId, bookId, body.get("quantity")));
    }

    @DeleteMapping("/items/{bookId}")
    public ResponseEntity<Cart> removeItem(
        @RequestHeader("X-User-Id") String userId,
        @PathVariable String bookId
    ) {
        return ResponseEntity.ok(cartService.removeItem(userId, bookId));
    }

    @DeleteMapping
    public ResponseEntity<Map<String, String>> clearCart(@RequestHeader("X-User-Id") String userId) {
        cartService.clearCart(userId);
        return ResponseEntity.ok(Map.of("message", "Cart cleared"));
    }
}
