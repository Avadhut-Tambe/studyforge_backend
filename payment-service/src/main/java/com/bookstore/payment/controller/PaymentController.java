package com.bookstore.payment.controller;

import com.bookstore.payment.dto.PaymentRequest;
import com.bookstore.payment.dto.PaymentResponse;
import com.bookstore.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/process")
    public ResponseEntity<PaymentResponse> processPayment(
        @RequestBody PaymentRequest request,
        @RequestHeader("X-User-Id") String userId
    ) {
        return ResponseEntity.ok(paymentService.processPayment(request, userId));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "payment-service"));
    }
}
