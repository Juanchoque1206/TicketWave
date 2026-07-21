package com.insert7team.TicketWave.event.dto;

import com.insert7team.TicketWave.common.enums.TicketType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record EventPricingRequest(
        @NotNull Long sectionId,
        @NotNull TicketType ticketType,
        @NotNull @Positive BigDecimal price,
        String currency,
        @Positive int availableQuantity
) {}
