package com.insert7team.TicketWave.payment.service;

import com.insert7team.TicketWave.common.enums.PaymentMethod;
import java.math.BigDecimal;
import java.util.Map;

public interface PaymentGateway {
    String processPayment(BigDecimal amount, String currency, PaymentMethod method, Map<String, String> metadata);
    String processRefund(String externalPaymentId, BigDecimal amount);
    boolean verifyWebhookSignature(String payload, String signature);
}
