package com.insert7team.TicketWave.fraud.kafka;

import java.time.LocalDateTime;

public record FraudAlertEvent(
        String eventId,
        String eventType,
        LocalDateTime timestamp,
        Long userId,
        Long orderId,
        String alertType,
        int riskScore,
        String details
) {}
