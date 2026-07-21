package com.insert7team.TicketWave.order.dto;

import com.insert7team.TicketWave.common.enums.TicketType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderItemRequest(
        @NotNull Long sectionId,
        @NotNull TicketType ticketType,
        Long seatId,
        @Positive int quantity
) {}
