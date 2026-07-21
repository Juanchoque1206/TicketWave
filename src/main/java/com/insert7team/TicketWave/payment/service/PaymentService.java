package com.insert7team.TicketWave.payment.service;

import com.insert7team.TicketWave.payment.dto.PaymentRequest;
import com.insert7team.TicketWave.payment.dto.PaymentResponse;
import com.insert7team.TicketWave.payment.dto.PaymentWebhookPayload;

public interface PaymentService {
    PaymentResponse initiatePayment(Long orderId, PaymentRequest request);
    PaymentResponse getPaymentStatus(Long orderId);
    void handlePaymentWebhook(PaymentWebhookPayload payload);
}
