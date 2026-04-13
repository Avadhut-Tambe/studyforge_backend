package com.bookstore.order.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cart {

    private String          userId;
    private List<CartItem>  items;
    private Instant         updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartItem {
        private String     bookId;
        private String     title;
        private String     author;
        private String     coverImageUrl;
        private int        quantity;
        private BigDecimal unitPrice;
        private String     sellerId;
    }

    public BigDecimal getTotal() {
        if (items == null) return BigDecimal.ZERO;
        return items.stream()
            .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getTotalItems() {
        if (items == null) return 0;
        return items.stream().mapToInt(CartItem::getQuantity).sum();
    }
}
