package com.insert7team.TicketWave.ticket.dto;

import com.insert7team.TicketWave.common.enums.TicketStatus;
import com.insert7team.TicketWave.common.enums.TicketType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TicketResponse(
        Long id,
        String ticketCode,
        Long eventId,
        String eventTitle,
        LocalDateTime eventDate,
        String venueName,
        String sectionName,
        String seatLabel,
        TicketType ticketType,
        TicketStatus status,
        BigDecimal price,
        LocalDateTime issuedAt
) {}
