package com.insert7team.TicketWave.promotion.entity;

import com.insert7team.TicketWave.shared.domain.model.AggregateRoot;
import com.insert7team.TicketWave.promotion.domain.PromotionScope;
import com.insert7team.TicketWave.promotion.domain.PromotionType;
import com.insert7team.TicketWave.shared.infrastructure.exception.BusinessRuleException;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Table(name = "promotions")
public class Promotion extends AggregateRoot {

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PromotionScope scope;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PromotionType type;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;

    @Column(nullable = false)
    private int maxUsages;

    @Column(nullable = false)
    private int currentUsages = 0;

    @Column(nullable = false)
    private LocalDateTime validFrom;

    @Column(nullable = false)
    private LocalDateTime validUntil;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "venue_id")
    private Long venueId;

    @Column(name = "event_id")
    private Long eventId;

    @Column(precision = 10, scale = 2)
    private BigDecimal minPurchaseAmount;

    // --- Domain behavior ---

    public boolean isValid(BigDecimal purchaseAmount) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(this.validFrom) || now.isAfter(this.validUntil)) {
            return false;
        }
        if (this.currentUsages >= this.maxUsages) {
            return false;
        }
        if (this.minPurchaseAmount != null && purchaseAmount.compareTo(this.minPurchaseAmount) < 0) {
            return false;
        }
        return this.active;
    }

    public String getValidationMessage(BigDecimal purchaseAmount) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(this.validFrom) || now.isAfter(this.validUntil)) {
            return "Promotion has expired";
        }
        if (this.currentUsages >= this.maxUsages) {
            return "Promotion max usages reached";
        }
        if (this.minPurchaseAmount != null && purchaseAmount.compareTo(this.minPurchaseAmount) < 0) {
            return "Minimum purchase amount not met";
        }
        if (!this.active) {
            return "Promotion is not active";
        }
        return "Valid";
    }

    public BigDecimal calculateDiscount(BigDecimal orderAmount) {
        return switch (this.type) {
            case PERCENTAGE -> orderAmount.multiply(this.discountValue)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            case FIXED_AMOUNT -> this.discountValue.min(orderAmount);
            case BUY_X_GET_Y -> BigDecimal.ZERO;
        };
    }

    public void incrementUsage() {
        if (this.currentUsages >= this.maxUsages) {
            throw new BusinessRuleException("Promotion max usages reached");
        }
        this.currentUsages++;
    }

    public void deactivate() {
        this.active = false;
    }

    // --- Getters and setters ---

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public PromotionScope getScope() { return scope; }
    public void setScope(PromotionScope scope) { this.scope = scope; }
    public PromotionType getType() { return type; }
    public void setType(PromotionType type) { this.type = type; }
    public BigDecimal getDiscountValue() { return discountValue; }
    public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }
    public int getMaxUsages() { return maxUsages; }
    public void setMaxUsages(int maxUsages) { this.maxUsages = maxUsages; }
    public int getCurrentUsages() { return currentUsages; }
    public void setCurrentUsages(int currentUsages) { this.currentUsages = currentUsages; }
    public LocalDateTime getValidFrom() { return validFrom; }
    public void setValidFrom(LocalDateTime validFrom) { this.validFrom = validFrom; }
    public LocalDateTime getValidUntil() { return validUntil; }
    public void setValidUntil(LocalDateTime validUntil) { this.validUntil = validUntil; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Long getVenueId() { return venueId; }
    public void setVenueId(Long venueId) { this.venueId = venueId; }
    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }
    public BigDecimal getMinPurchaseAmount() { return minPurchaseAmount; }
    public void setMinPurchaseAmount(BigDecimal minPurchaseAmount) { this.minPurchaseAmount = minPurchaseAmount; }
}
