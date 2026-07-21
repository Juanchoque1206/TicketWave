package com.insert7team.TicketWave.venue.dto;

import java.util.List;

public record SeatMapResponse(
        Long venueId,
        String venueName,
        boolean hasAssignedSeating,
        List<SectionWithSeatsResponse> sections
) {}
