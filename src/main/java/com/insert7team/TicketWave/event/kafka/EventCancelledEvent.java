package com.insert7team.TicketWave.event.kafka;

import java.time.LocalDateTime;
import java.util.List;

public record EventCancelledEvent(
        String eventId,
        String eventType,
        LocalDateTime timestamp,
        Long eventEntityId,
        String title,
        String reason,
        List<Long> affectedUserIds
) {}
