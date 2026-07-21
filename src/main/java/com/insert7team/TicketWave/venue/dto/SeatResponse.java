package com.insert7team.TicketWave.venue.dto;

public record SeatResponse(
        Long id,
        String row,
        int number,
        String label
) {}
