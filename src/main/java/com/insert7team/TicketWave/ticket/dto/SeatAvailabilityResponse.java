package com.insert7team.TicketWave.ticket.dto;

import java.util.List;

public record SeatAvailabilityResponse(
        Long eventId,
        Long sectionId,
        String sectionName,
        boolean generalAdmission,
        int totalCapacity,
        int availableCount,
        List<SeatStatusResponse> seats
) {}
