package com.insert7team.TicketWave.event.dto;

import com.insert7team.TicketWave.common.enums.EventCategory;
import java.time.LocalDateTime;
import java.util.Objects;

public record EventSearchCriteria(
        String city,
        String artist,
        Long venueId,
        LocalDateTime dateFrom,
        LocalDateTime dateTo,
        EventCategory category
) {
    public String toCacheKey() {
        return String.valueOf(Objects.hash(city, artist, venueId, dateFrom, dateTo, category));
    }
}
