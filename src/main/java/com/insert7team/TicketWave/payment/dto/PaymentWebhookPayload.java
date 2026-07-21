package com.insert7team.TicketWave.payment.dto;

public record PaymentWebhookPayload(
        String externalPaymentId,
        String status,
        String failureReason,
        String signature,
        String rawPayload
) {}
