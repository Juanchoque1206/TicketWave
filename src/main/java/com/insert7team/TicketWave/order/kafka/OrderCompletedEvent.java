package com.insert7team.TicketWave.order.kafka;

import java.time.LocalDateTime;
import java.util.List;

public record OrderCompletedEvent(
        String eventId,
        String eventType,
        LocalDateTime timestamp,
        Long orderId,
        Long userId,
        String orderNumber,
        List<String> ticketCodes
) {}
