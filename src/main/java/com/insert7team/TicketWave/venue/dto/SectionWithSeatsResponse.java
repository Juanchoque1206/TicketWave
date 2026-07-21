package com.insert7team.TicketWave.venue.dto;

import java.util.List;

public record SectionWithSeatsResponse(
        Long sectionId,
        String sectionName,
        boolean generalAdmission,
        int capacity,
        List<SeatResponse> seats
) {}
