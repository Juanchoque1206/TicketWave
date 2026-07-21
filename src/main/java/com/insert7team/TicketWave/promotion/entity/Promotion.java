package com.insert7team.TicketWave.promotion.entity;

import com.insert7team.TicketWave.common.entity.BaseEntity;
import com.insert7team.TicketWave.common.enums.PromotionScope;
import com.insert7team.TicketWave.common.enums.PromotionType;
import com.insert7team.TicketWave.event.entity.Event;
import com.insert7team.TicketWave.venue.entity.Venue;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "promotions")
public class Promotion extends BaseEntity {

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id")
    private Venue venue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @Column(precision = 10, scale = 2)
    private BigDecimal minPurchaseAmount;

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
    public Venue getVenue() { return venue; }
    public void setVenue(Venue venue) { this.venue = venue; }
    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }
    public BigDecimal getMinPurchaseAmount() { return minPurchaseAmount; }
    public void setMinPurchaseAmount(BigDecimal minPurchaseAmount) { this.minPurchaseAmount = minPurchaseAmount; }
}
