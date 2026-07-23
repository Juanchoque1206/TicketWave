package com.insert7team.TicketWave.event.dto;

import com.insert7team.TicketWave.event.domain.EventCategory;
import com.insert7team.TicketWave.event.domain.EventStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EventSummaryResponse(
        Long id,
        String title,
        String artist,
        EventCategory category,
        LocalDateTime eventDate,
        EventStatus status,
        String venueName,
        String city,
        BigDecimal minPrice,
        String imageUrl
) {}
