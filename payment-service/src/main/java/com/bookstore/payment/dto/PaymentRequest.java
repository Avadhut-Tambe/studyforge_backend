package com.bookstore.payment.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentRequest {

    private String     orderId;
    private BigDecimal amount;
    private String     method;        // CARD | UPI | NETBANKING | COD

    // Card details (all dummy — never stored)
    private String     cardNumber;
    private String     cardExpiry;
    private String     cardCvv;
    private String     cardHolder;

    // UPI
    private String     upiId;
}
