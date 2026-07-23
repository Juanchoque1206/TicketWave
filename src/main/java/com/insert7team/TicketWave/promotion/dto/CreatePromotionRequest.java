package com.insert7team.TicketWave.promotion.dto;

import com.insert7team.TicketWave.promotion.domain.PromotionScope;
import com.insert7team.TicketWave.promotion.domain.PromotionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreatePromotionRequest(
        @NotBlank String code,
        String description,
        @NotNull PromotionScope scope,
        @NotNull PromotionType type,
        @NotNull @Positive BigDecimal discountValue,
        @Positive int maxUsages,
        @NotNull LocalDateTime validFrom,
        @NotNull LocalDateTime validUntil,
        Long venueId,
        Long eventId,
        BigDecimal minPurchaseAmount
) {}
