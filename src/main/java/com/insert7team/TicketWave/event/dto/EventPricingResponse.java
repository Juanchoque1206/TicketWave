package com.insert7team.TicketWave.event.dto;

import com.insert7team.TicketWave.ticket.domain.TicketType;
import java.math.BigDecimal;

public record EventPricingResponse(
        Long id,
        Long sectionId,
        String sectionName,
        TicketType ticketType,
        BigDecimal price,
        String currency,
        int availableQuantity
) {}
