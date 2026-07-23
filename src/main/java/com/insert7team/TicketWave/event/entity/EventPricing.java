package com.insert7team.TicketWave.event.entity;

import com.insert7team.TicketWave.shared.infrastructure.persistence.BaseEntity;
import com.insert7team.TicketWave.ticket.domain.TicketType;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "event_pricings", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"event_id", "section_id", "ticketType"})
})
public class EventPricing extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "section_id", nullable = false)
    private Long sectionId;

    @Column(length = 100)
    private String sectionName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketType ticketType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 3)
    private String currency = "USD";

    @Column(nullable = false)
    private int availableQuantity;

    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }
    public Long getSectionId() { return sectionId; }
    public void setSectionId(Long sectionId) { this.sectionId = sectionId; }
    public String getSectionName() { return sectionName; }
    public void setSectionName(String sectionName) { this.sectionName = sectionName; }
    public TicketType getTicketType() { return ticketType; }
    public void setTicketType(TicketType ticketType) { this.ticketType = ticketType; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public int getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(int availableQuantity) { this.availableQuantity = availableQuantity; }
}
