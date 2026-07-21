package com.insert7team.TicketWave.venue.dto;

import java.time.LocalDateTime;

public record VenueResponse(
        Long id,
        String name,
        String city,
        String address,
        String country,
        int totalCapacity,
        boolean hasAssignedSeating,
        int sectionCount,
        LocalDateTime createdAt
) {}
