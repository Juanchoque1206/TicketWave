package com.insert7team.TicketWave.venue.dto;

public record SectionResponse(
        Long id,
        String name,
        int capacity,
        boolean generalAdmission,
        int seatCount
) {}
