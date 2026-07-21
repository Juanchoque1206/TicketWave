package com.insert7team.TicketWave.promotion.service;

import com.insert7team.TicketWave.common.enums.PromotionScope;
import com.insert7team.TicketWave.promotion.dto.CreatePromotionRequest;
import com.insert7team.TicketWave.promotion.dto.PromotionResponse;
import com.insert7team.TicketWave.promotion.dto.PromotionValidationResponse;
import java.math.BigDecimal;
import java.util.List;

public interface PromotionService {
    PromotionResponse createPromotion(CreatePromotionRequest request);
    PromotionValidationResponse validatePromotion(String code, Long eventId, BigDecimal orderAmount);
    BigDecimal applyDiscount(String code, BigDecimal subtotal, Long eventId);
    List<PromotionResponse> getActivePromotions(PromotionScope scope);
    List<PromotionResponse> getVenuePromotions(Long venueId);
    void deactivatePromotion(Long promotionId);
}
