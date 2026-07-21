package com.insert7team.TicketWave.promotion.service;

import com.insert7team.TicketWave.common.enums.PromotionScope;
import com.insert7team.TicketWave.common.enums.PromotionType;
import com.insert7team.TicketWave.common.exception.BusinessRuleException;
import com.insert7team.TicketWave.common.exception.DuplicateResourceException;
import com.insert7team.TicketWave.common.exception.ResourceNotFoundException;
import com.insert7team.TicketWave.event.entity.Event;
import com.insert7team.TicketWave.event.repository.EventRepository;
import com.insert7team.TicketWave.promotion.dto.CreatePromotionRequest;
import com.insert7team.TicketWave.promotion.dto.PromotionResponse;
import com.insert7team.TicketWave.promotion.dto.PromotionValidationResponse;
import com.insert7team.TicketWave.promotion.entity.Promotion;
import com.insert7team.TicketWave.promotion.repository.PromotionRepository;
import com.insert7team.TicketWave.venue.entity.Venue;
import com.insert7team.TicketWave.venue.repository.VenueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promotionRepository;
    private final VenueRepository venueRepository;
    private final EventRepository eventRepository;

    public PromotionServiceImpl(PromotionRepository promotionRepository,
                                VenueRepository venueRepository,
                                EventRepository eventRepository) {
        this.promotionRepository = promotionRepository;
        this.venueRepository = venueRepository;
        this.eventRepository = eventRepository;
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

        if (request.scope() == PromotionScope.VENUE_SPECIFIC && request.venueId() != null) {
            Venue venue = venueRepository.findById(request.venueId())
                    .orElseThrow(() -> new ResourceNotFoundException("Venue not found"));
            promotion.setVenue(venue);
        }
        if (request.scope() == PromotionScope.EVENT_SPECIFIC && request.eventId() != null) {
            Event event = eventRepository.findById(request.eventId())
                    .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
            promotion.setEvent(event);
        }
        promotion.setMinPurchaseAmount(request.minPurchaseAmount());
        promotion = promotionRepository.save(promotion);

        return toPromotionResponse(promotion);
    }

    @Override
    public PromotionValidationResponse validatePromotion(String code, Long eventId, BigDecimal orderAmount) {
        Promotion promotion = promotionRepository.findByCodeAndActiveTrue(code.toUpperCase()).orElse(null);
        if (promotion == null) {
            return new PromotionValidationResponse(false, "Promotion code not found", BigDecimal.ZERO, null);
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(promotion.getValidFrom()) || now.isAfter(promotion.getValidUntil())) {
            return new PromotionValidationResponse(false, "Promotion has expired", BigDecimal.ZERO, promotion.getType());
        }
        if (promotion.getCurrentUsages() >= promotion.getMaxUsages()) {
            return new PromotionValidationResponse(false, "Promotion max usages reached", BigDecimal.ZERO, promotion.getType());
        }
        if (promotion.getMinPurchaseAmount() != null && orderAmount.compareTo(promotion.getMinPurchaseAmount()) < 0) {
            return new PromotionValidationResponse(false, "Minimum purchase amount not met", BigDecimal.ZERO, promotion.getType());
        }

        BigDecimal discount = calculateDiscount(promotion, orderAmount);
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
        promotion.setCurrentUsages(promotion.getCurrentUsages() + 1);
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
        promotion.setActive(false);
        promotionRepository.save(promotion);
    }

    private BigDecimal calculateDiscount(Promotion promotion, BigDecimal orderAmount) {
        return switch (promotion.getType()) {
            case PERCENTAGE -> orderAmount.multiply(promotion.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            case FIXED_AMOUNT -> promotion.getDiscountValue().min(orderAmount);
            case BUY_X_GET_Y -> BigDecimal.ZERO; // simplified
        };
    }

    private PromotionResponse toPromotionResponse(Promotion promotion) {
        return new PromotionResponse(
                promotion.getId(), promotion.getCode(), promotion.getDescription(),
                promotion.getScope(), promotion.getType(), promotion.getDiscountValue(),
                promotion.getMaxUsages(), promotion.getCurrentUsages(),
                promotion.getValidFrom(), promotion.getValidUntil(), promotion.isActive(),
                promotion.getVenue() != null ? promotion.getVenue().getName() : null,
                promotion.getEvent() != null ? promotion.getEvent().getTitle() : null
        );
    }
}
