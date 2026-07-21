package com.insert7team.TicketWave.event.dto;

import com.insert7team.TicketWave.common.enums.EventCategory;
import java.time.LocalDateTime;

public record UpdateEventRequest(
        String title,
        String description,
        String artist,
        EventCategory category,
        LocalDateTime eventDate,
        LocalDateTime doorsOpenAt,
        String imageUrl
) {}
