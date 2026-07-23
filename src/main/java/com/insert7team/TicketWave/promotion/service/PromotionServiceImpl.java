package com.insert7team.TicketWave.promotion.service;

import com.insert7team.TicketWave.promotion.domain.PromotionScope;
import com.insert7team.TicketWave.shared.infrastructure.exception.BusinessRuleException;
import com.insert7team.TicketWave.shared.infrastructure.exception.DuplicateResourceException;
import com.insert7team.TicketWave.shared.infrastructure.exception.ResourceNotFoundException;
import com.insert7team.TicketWave.promotion.dto.CreatePromotionRequest;
import com.insert7team.TicketWave.promotion.dto.PromotionResponse;
import com.insert7team.TicketWave.promotion.dto.PromotionValidationResponse;
import com.insert7team.TicketWave.promotion.entity.Promotion;
import com.insert7team.TicketWave.promotion.repository.PromotionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promotionRepository;

    public PromotionServiceImpl(PromotionRepository promotionRepository) {
        this.promotionRepository = promotionRepository;
    }

    @Override
    @Transactional
    public PromotionResponse createPromotion(CreatePromotionRequest request) {
        if (promotionRepository.findByCodeAndActiveTrue(request.code()).isPresent()) {
            throw new DuplicateResourceException("Promotion code already exists: " + request.code());
        }

        Promotion promotion = new Promotion();
        promotion.setCode(request.code().toUpperCase());
        promotion.setDescription(request.description());
        promotion.setScope(request.scope());
        promotion.setType(request.type());
        promotion.setDiscountValue(request.discountValue());
        promotion.setMaxUsages(request.maxUsages());
        promotion.setValidFrom(request.validFrom());
        promotion.setValidUntil(request.validUntil());
        promotion.setMinPurchaseAmount(request.minPurchaseAmount());

        if (request.scope() == PromotionScope.VENUE_SPECIFIC && request.venueId() != null) {
            promotion.setVenueId(request.venueId());
        }
        if (request.scope() == PromotionScope.EVENT_SPECIFIC && request.eventId() != null) {
            promotion.setEventId(request.eventId());
        }

        promotion = promotionRepository.save(promotion);
        return toPromotionResponse(promotion);
    }

    @Override
    public PromotionValidationResponse validatePromotion(String code, Long eventId, BigDecimal orderAmount) {
        Promotion promotion = promotionRepository.findByCodeAndActiveTrue(code.toUpperCase()).orElse(null);
        if (promotion == null) {
            return new PromotionValidationResponse(false, "Promotion code not found", BigDecimal.ZERO, null);
        }

        if (!promotion.isValid(orderAmount)) {
            String message = promotion.getValidationMessage(orderAmount);
            return new PromotionValidationResponse(false, message, BigDecimal.ZERO, promotion.getType());
        }

        BigDecimal discount = promotion.calculateDiscount(orderAmount);
        return new PromotionValidationResponse(true, "Valid", discount, promotion.getType());
    }

    @Override
    @Transactional
    public BigDecimal applyDiscount(String code, BigDecimal subtotal, Long eventId) {
        PromotionValidationResponse validation = validatePromotion(code, eventId, subtotal);
        if (!validation.valid()) {
            throw new BusinessRuleException("Invalid promotion: " + validation.message());
        }

        Promotion promotion = promotionRepository.findByCodeAndActiveTrue(code.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found"));
        promotion.incrementUsage();
        promotionRepository.save(promotion);

        return validation.discountAmount();
    }

    @Override
    public List<PromotionResponse> getActivePromotions(PromotionScope scope) {
        return promotionRepository.findByScopeAndActiveTrue(scope).stream()
                .map(this::toPromotionResponse)
                .toList();
    }

    @Override
    public List<PromotionResponse> getVenuePromotions(Long venueId) {
        return promotionRepository.findByVenueIdAndActiveTrue(venueId).stream()
                .map(this::toPromotionResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deactivatePromotion(Long promotionId) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found"));
        promotion.deactivate();
        promotionRepository.save(promotion);
    }

    private PromotionResponse toPromotionResponse(Promotion promotion) {
        return new PromotionResponse(
                promotion.getId(), promotion.getCode(), promotion.getDescription(),
                promotion.getScope(), promotion.getType(), promotion.getDiscountValue(),
                promotion.getMaxUsages(), promotion.getCurrentUsages(),
                promotion.getValidFrom(), promotion.getValidUntil(), promotion.isActive(),
                null, null
        );
    }
}
