package com.bookstore.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private String     paymentId;
    private String     orderId;
    private String     status;        // SUCCESS | FAILED | PENDING
    private BigDecimal amount;
    private String     method;
    private String     message;
    private Instant    processedAt;

    // For COD
    private String     transactionRef;
}
