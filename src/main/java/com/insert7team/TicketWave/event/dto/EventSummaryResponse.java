package com.insert7team.TicketWave.event.dto;

import com.insert7team.TicketWave.common.enums.EventCategory;
import com.insert7team.TicketWave.common.enums.EventStatus;
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
