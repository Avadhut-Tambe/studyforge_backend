package com.bookstore.order.service;

import com.bookstore.order.model.Cart;
import com.bookstore.order.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;

    public Cart getCart(String userId) {
        return cartRepository.getCart(userId);
    }

    public Cart addItem(String userId, Cart.CartItem item) {
        Cart cart = cartRepository.getCart(userId);
        if (cart.getItems() == null) cart.setItems(new ArrayList<>());

        // If book already in cart, increment quantity
        Optional<Cart.CartItem> existing = cart.getItems().stream()
            .filter(i -> i.getBookId().equals(item.getBookId()))
            .findFirst();

        if (existing.isPresent()) {
            existing.get().setQuantity(existing.get().getQuantity() + item.getQuantity());
        } else {
            cart.getItems().add(item);
        }

        return cartRepository.saveCart(cart);
    }

    public Cart updateItemQuantity(String userId, String bookId, int quantity) {
        Cart cart = cartRepository.getCart(userId);
        if (quantity <= 0) {
            cart.getItems().removeIf(i -> i.getBookId().equals(bookId));
        } else {
            cart.getItems().stream()
                .filter(i -> i.getBookId().equals(bookId))
                .findFirst()
                .ifPresent(i -> i.setQuantity(quantity));
        }
        return cartRepository.saveCart(cart);
    }

    public Cart removeItem(String userId, String bookId) {
        Cart cart = cartRepository.getCart(userId);
        cart.getItems().removeIf(i -> i.getBookId().equals(bookId));
        return cartRepository.saveCart(cart);
    }

    public void clearCart(String userId) {
        cartRepository.clearCart(userId);
    }
}
