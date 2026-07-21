package com.insert7team.TicketWave.ticket.dto;

import com.insert7team.TicketWave.common.enums.SeatStatus;

public record SeatStatusResponse(
        Long seatId,
        String row,
        int number,
        String label,
        SeatStatus status
) {}
