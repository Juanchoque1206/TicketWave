package com.insert7team.TicketWave.promotion.dto;

import com.insert7team.TicketWave.common.enums.PromotionType;
import java.math.BigDecimal;

public record PromotionValidationResponse(
        boolean valid,
        String message,
        BigDecimal discountAmount,
        PromotionType type
) {}
