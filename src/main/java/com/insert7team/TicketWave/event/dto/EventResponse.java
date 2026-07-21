package com.insert7team.TicketWave.event.dto;

import com.insert7team.TicketWave.common.enums.EventCategory;
import com.insert7team.TicketWave.common.enums.EventStatus;
import com.insert7team.TicketWave.venue.dto.VenueResponse;
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
        VenueResponse venue,
        String imageUrl,
        List<EventPricingResponse> pricings,
        LocalDateTime createdAt
) {}
