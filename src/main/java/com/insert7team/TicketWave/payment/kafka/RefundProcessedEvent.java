package com.insert7team.TicketWave.payment.kafka;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RefundProcessedEvent(
        String eventId,
        String eventType,
        LocalDateTime timestamp,
        Long refundId,
        Long orderId,
        Long userId,
        String email,
        BigDecimal amount,
        String reason
) {}
