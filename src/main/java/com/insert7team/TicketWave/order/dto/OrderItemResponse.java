package com.insert7team.TicketWave.order.dto;

import com.insert7team.TicketWave.common.enums.TicketType;
import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        Long eventId,
        String eventTitle,
        Long sectionId,
        String sectionName,
        Long seatId,
        String seatLabel,
        TicketType ticketType,
        BigDecimal unitPrice,
        int quantity
) {}
