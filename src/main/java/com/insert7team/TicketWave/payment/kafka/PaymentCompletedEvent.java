package com.insert7team.TicketWave.payment.kafka;

import com.insert7team.TicketWave.payment.domain.PaymentMethod;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentCompletedEvent(
        String eventId,
        String eventType,
        LocalDateTime timestamp,
        Long paymentId,
        Long orderId,
        BigDecimal amount,
        PaymentMethod paymentMethod
) {}
