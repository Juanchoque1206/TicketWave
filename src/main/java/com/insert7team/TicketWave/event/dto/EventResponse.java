package com.insert7team.TicketWave.event.dto;

import com.insert7team.TicketWave.event.domain.EventCategory;
import com.insert7team.TicketWave.event.domain.EventStatus;
import java.time.LocalDateTime;
import java.util.List;

public record EventResponse(
        Long id,
        String title,
        String description,
        String artist,
        EventCategory category,
        LocalDateTime eventDate,
        LocalDateTime doorsOpenAt,
        EventStatus status,
        int maxTicketsPerUser,
        LocalDateTime salesStartAt,
        LocalDateTime salesEndAt,
        Long venueId,
        String venueName,
        String venueCity,
        Long organizerId,
        String imageUrl,
        List<EventPricingResponse> pricings,
        LocalDateTime createdAt
) {}
