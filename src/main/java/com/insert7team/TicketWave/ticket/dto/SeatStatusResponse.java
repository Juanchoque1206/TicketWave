package com.insert7team.TicketWave.ticket.dto;

import com.insert7team.TicketWave.venue.domain.SeatStatus;

public record SeatStatusResponse(
        Long seatId,
        String row,
        int number,
        String label,
        SeatStatus status
) {}
