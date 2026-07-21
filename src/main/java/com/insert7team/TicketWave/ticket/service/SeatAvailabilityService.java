package com.insert7team.TicketWave.ticket.service;

import com.insert7team.TicketWave.common.enums.SeatStatus;
import com.insert7team.TicketWave.ticket.dto.SeatAvailabilityResponse;

import java.time.Duration;

public interface SeatAvailabilityService {
    boolean reserveSeat(Long eventId, Long seatId, Long userId, Duration holdDuration);
    void releaseSeat(Long eventId, Long seatId);
    void confirmSeat(Long eventId, Long seatId);
    SeatStatus getSeatStatus(Long eventId, Long seatId);
    SeatAvailabilityResponse getAvailability(Long eventId, Long sectionId);
    int getAvailableCountGA(Long eventId, Long sectionId);
    boolean reserveGA(Long eventId, Long sectionId, int quantity, Long userId, Duration holdDuration);
    void releaseGA(Long eventId, Long sectionId, int quantity);
    void confirmGA(Long eventId, Long sectionId, int quantity);
    void initializeEventAvailability(Long eventId);
}
