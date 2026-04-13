package com.bookstore.order.dto;

import com.bookstore.order.model.Order;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CheckoutRequest {

    @NotNull(message = "Shipping address is required")
    private Order.Address shippingAddress;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod; // CARD | UPI | NETBANKING | COD
}
