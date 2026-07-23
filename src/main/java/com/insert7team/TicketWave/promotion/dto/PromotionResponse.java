package com.insert7team.TicketWave.promotion.dto;

import com.insert7team.TicketWave.promotion.domain.PromotionScope;
import com.insert7team.TicketWave.promotion.domain.PromotionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PromotionResponse(
        Long id,
        String code,
        String description,
        PromotionScope scope,
        PromotionType type,
        BigDecimal discountValue,
        int maxUsages,
        int currentUsages,
        LocalDateTime validFrom,
        LocalDateTime validUntil,
        boolean active,
        String venueName,
        String eventTitle
) {}
