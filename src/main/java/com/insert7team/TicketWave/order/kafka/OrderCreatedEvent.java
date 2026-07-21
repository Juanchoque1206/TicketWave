package com.insert7team.TicketWave.order.kafka;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderCreatedEvent(
        String eventId,
        String eventType,
        LocalDateTime timestamp,
        Long orderId,
        Long userId,
        BigDecimal totalAmount,
        int itemCount,
        List<Long> eventIds
) {}
