package com.bookstore.payment.service;

import com.bookstore.payment.dto.PaymentRequest;
import com.bookstore.payment.dto.PaymentResponse;
import com.google.cloud.firestore.CollectionReference;
import com.google.firebase.cloud.FirestoreClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * Mock payment service.
 * Simulates payment processing with a configurable failure rate.
 * In production, this would integrate with Razorpay, Stripe, etc.
 *
 * After "processing", it calls the order-service to update order status.
 */
@Slf4j
@Service
public class PaymentService {

    @Value("${payment.mock.failure-rate:0.1}")   // 10% failure rate for realism
    private double failureRate;

    @Value("${payment.mock.delay-ms:1500}")       // Simulated processing delay
    private long delayMs;

    @Value("${services.order-service-url:http://localhost:8084}")
    private String orderServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final Random       random       = new Random();

    public PaymentResponse processPayment(PaymentRequest request, String userId) {
        log.info("Processing {} payment for order {} amount {}",
                 request.getMethod(), request.getOrderId(), request.getAmount());

        // Simulate processing delay
        simulateDelay();

        String paymentId = "PAY_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        boolean success  = !shouldFail(request);

        PaymentResponse response = PaymentResponse.builder()
            .paymentId(paymentId)
            .orderId(request.getOrderId())
            .amount(request.getAmount())
            .method(request.getMethod())
            .processedAt(Instant.now())
            .transactionRef("TXN_" + System.currentTimeMillis())
            .build();

        if (success) {
            response.setStatus("SUCCESS");
            response.setMessage("Payment successful");
            persistPayment(response, userId);
            notifyOrderService(request.getOrderId(), "CONFIRMED", paymentId);
        } else {
            response.setStatus("FAILED");
            response.setMessage("Payment declined. Please check your details and try again.");
            notifyOrderService(request.getOrderId(), "PAYMENT_FAILED", null);
        }

        return response;
    }

    private boolean shouldFail(PaymentRequest request) {
        // Always fail if test card 4111111111111111 with wrong CVV
        if ("CARD".equals(request.getMethod()) && "000".equals(request.getCardCvv())) {
            return true;
        }
        // UPI failure simulation: wrong UPI format
        if ("UPI".equals(request.getMethod()) && request.getUpiId() != null
                && !request.getUpiId().contains("@")) {
            return true;
        }
        return random.nextDouble() < failureRate;
    }

    private void persistPayment(PaymentResponse response, String userId) {
        try {
            CollectionReference col = FirestoreClient.getFirestore().collection("payments");
            Map<String, Object> data = new HashMap<>();
            data.put("paymentId",      response.getPaymentId());
            data.put("orderId",        response.getOrderId());
            data.put("userId",         userId);
            data.put("status",         response.getStatus());
            data.put("amount",         response.getAmount().doubleValue());
            data.put("method",         response.getMethod());
            data.put("transactionRef", response.getTransactionRef());
            data.put("processedAt",    response.getProcessedAt().toEpochMilli());
            col.document(response.getPaymentId()).set(data).get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            log.error("Failed to persist payment record", e);
        }
    }

    private void notifyOrderService(String orderId, String status, String paymentId) {
        try {
            String url = orderServiceUrl + "/api/orders/" + orderId + "/status";
            Map<String, String> body = new HashMap<>();
            body.put("status", status);
            if (paymentId != null) body.put("paymentId", paymentId);
            restTemplate.patchForObject(url, body, Map.class);
            log.info("Order {} updated to status {}", orderId, status);
        } catch (Exception e) {
            log.error("Failed to notify order-service for order {}: {}", orderId, e.getMessage());
        }
    }

    private void simulateDelay() {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
