package com.insert7team.TicketWave.event.kafka;

import java.time.LocalDateTime;

public record EventChangedEvent(
        String eventId,
        String eventType,
        LocalDateTime timestamp,
        Long eventEntityId,
        String title,
        String changeType,
        String description,
        int affectedTicketCount
) {}
