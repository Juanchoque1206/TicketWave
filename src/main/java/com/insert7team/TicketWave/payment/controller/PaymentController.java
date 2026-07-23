package com.insert7team.TicketWave.payment.controller;

import com.insert7team.TicketWave.shared.domain.dto.ApiResponse;
import com.insert7team.TicketWave.payment.dto.*;
import com.insert7team.TicketWave.payment.service.PaymentService;
import com.insert7team.TicketWave.payment.service.RefundService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PaymentController {

    private final PaymentService paymentService;
    private final RefundService refundService;

    public PaymentController(PaymentService paymentService, RefundService refundService) {
        this.paymentService = paymentService;
        this.refundService = refundService;
    }

    @PostMapping("/payments/orders/{orderId}/pay")
    public ResponseEntity<ApiResponse<PaymentResponse>> initiatePayment(
            @PathVariable Long orderId, @Valid @RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.initiatePayment(orderId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Payment initiated", response));
    }

    @GetMapping("/payments/orders/{orderId}/status")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentStatus(@PathVariable Long orderId) {
        PaymentResponse response = paymentService.getPaymentStatus(orderId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/payments/webhook")
    public ResponseEntity<ApiResponse<Void>> handleWebhook(@RequestBody PaymentWebhookPayload payload) {
        paymentService.handlePaymentWebhook(payload);
        return ResponseEntity.ok(ApiResponse.ok("Webhook processed", null));
    }

    @PostMapping("/refunds/orders/{orderId}")
    public ResponseEntity<ApiResponse<RefundResponse>> requestRefund(
            @PathVariable Long orderId, @Valid @RequestBody RefundRequest request) {
        RefundResponse response = refundService.requestRefund(orderId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Refund processed", response));
    }

    @GetMapping("/refunds/{refundId}")
    public ResponseEntity<ApiResponse<RefundResponse>> getRefundStatus(@PathVariable Long refundId) {
        RefundResponse response = refundService.getRefundStatus(refundId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/refunds/orders/{orderId}")
    public ResponseEntity<ApiResponse<List<RefundResponse>>> getRefundsForOrder(@PathVariable Long orderId) {
        List<RefundResponse> response = refundService.getRefundsForOrder(orderId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
