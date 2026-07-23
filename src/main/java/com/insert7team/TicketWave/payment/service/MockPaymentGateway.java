package com.insert7team.TicketWave.payment.service;

import com.insert7team.TicketWave.payment.domain.PaymentMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Component
public class MockPaymentGateway implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(MockPaymentGateway.class);

    @Override
    public String processPayment(BigDecimal amount, String currency, PaymentMethod method, Map<String, String> metadata) {
        String transactionId = "PAY-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        log.info("Mock payment processed: {} {} via {} -> {}", amount, currency, method, transactionId);
        return transactionId;
    }

    @Override
    public String processRefund(String externalPaymentId, BigDecimal amount) {
        String refundId = "REF-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        log.info("Mock refund processed: {} for payment {} -> {}", amount, externalPaymentId, refundId);
        return refundId;
    }

    @Override
    public boolean verifyWebhookSignature(String payload, String signature) {
        log.info("Mock webhook signature verification (always returns true)");
        return true;
    }
}
