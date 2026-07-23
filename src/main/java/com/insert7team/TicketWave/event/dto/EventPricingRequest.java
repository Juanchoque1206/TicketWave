package com.insert7team.TicketWave.event.dto;

import com.insert7team.TicketWave.ticket.domain.TicketType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record EventPricingRequest(
        @NotNull Long sectionId,
        String sectionName,
        @NotNull TicketType ticketType,
        @NotNull @Positive BigDecimal price,
        String currency,
        @Positive int availableQuantity
) {}
