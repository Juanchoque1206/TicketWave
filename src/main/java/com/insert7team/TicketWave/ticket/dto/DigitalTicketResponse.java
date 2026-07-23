package com.insert7team.TicketWave.ticket.dto;

import com.insert7team.TicketWave.ticket.domain.TicketStatus;
import java.time.LocalDateTime;

public record DigitalTicketResponse(
        String ticketCode,
        String qrCodeData,
        String eventTitle,
        LocalDateTime eventDate,
        String venueName,
        String venueAddress,
        String sectionName,
        String seatLabel,
        String holderName,
        TicketStatus status
) {}
