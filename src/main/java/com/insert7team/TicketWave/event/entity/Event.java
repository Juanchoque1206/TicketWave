package com.insert7team.TicketWave.event.entity;

import com.insert7team.TicketWave.shared.domain.model.AggregateRoot;
import com.insert7team.TicketWave.event.domain.EventCategory;
import com.insert7team.TicketWave.event.domain.EventStatus;
import com.insert7team.TicketWave.shared.infrastructure.exception.BusinessRuleException;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "events", indexes = {
        @Index(name = "idx_event_date", columnList = "eventDate"),
        @Index(name = "idx_event_status", columnList = "status")
})
public class Event extends AggregateRoot {

    @Column(nullable = false, length = 300)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 200)
    private String artist;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventCategory category;

    @Column(nullable = false)
    private LocalDateTime eventDate;

    private LocalDateTime doorsOpenAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status;

    @Column(nullable = false)
    private int maxTicketsPerUser = 6;

    @Column(nullable = false)
    private LocalDateTime salesStartAt;

    @Column(nullable = false)
    private LocalDateTime salesEndAt;

    @Column(name = "venue_id", nullable = false)
    private Long venueId;

    @Column(length = 200)
    private String venueName;

    @Column(length = 100)
    private String venueCity;

    @Column(name = "organizer_id", nullable = false)
    private Long organizerId;

    @Column(length = 500)
    private String imageUrl;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventPricing> pricings = new ArrayList<>();

    // --- Domain behavior ---

    public void publish() {
        if (this.status != EventStatus.DRAFT) {
            throw new BusinessRuleException("Only draft events can be published");
        }
        if (this.salesStartAt == null || this.salesEndAt == null) {
            throw new BusinessRuleException("Sales start and end dates must be set before publishing");
        }
        this.status = EventStatus.ON_SALE;
    }

    public void cancel(String reason) {
        if (this.status == EventStatus.CANCELLED) {
            throw new BusinessRuleException("Event is already cancelled");
        }
        this.status = EventStatus.CANCELLED;
    }

    public void postpone(LocalDateTime newDate) {
        if (this.status == EventStatus.CANCELLED || this.status == EventStatus.COMPLETED) {
            throw new BusinessRuleException("Cannot postpone a cancelled or completed event");
        }
        this.status = EventStatus.POSTPONED;
        this.eventDate = newDate;
    }

    public void complete() {
        this.status = EventStatus.COMPLETED;
    }

    public boolean isSalesActive() {
        if (this.status != EventStatus.ON_SALE) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(this.salesStartAt) && !now.isAfter(this.salesEndAt);
    }

    public void updateDetails(String title, String description, String artist,
                              EventCategory category, LocalDateTime eventDate,
                              LocalDateTime doorsOpenAt, String imageUrl) {
        if (title != null) this.title = title;
        if (description != null) this.description = description;
        if (artist != null) this.artist = artist;
        if (category != null) this.category = category;
        if (eventDate != null) this.eventDate = eventDate;
        if (doorsOpenAt != null) this.doorsOpenAt = doorsOpenAt;
        if (imageUrl != null) this.imageUrl = imageUrl;
    }

    // --- Getters and setters ---

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }
    public EventCategory getCategory() { return category; }
    public void setCategory(EventCategory category) { this.category = category; }
    public LocalDateTime getEventDate() { return eventDate; }
    public void setEventDate(LocalDateTime eventDate) { this.eventDate = eventDate; }
    public LocalDateTime getDoorsOpenAt() { return doorsOpenAt; }
    public void setDoorsOpenAt(LocalDateTime doorsOpenAt) { this.doorsOpenAt = doorsOpenAt; }
    public EventStatus getStatus() { return status; }
    public void setStatus(EventStatus status) { this.status = status; }
    public int getMaxTicketsPerUser() { return maxTicketsPerUser; }
    public void setMaxTicketsPerUser(int maxTicketsPerUser) { this.maxTicketsPerUser = maxTicketsPerUser; }
    public LocalDateTime getSalesStartAt() { return salesStartAt; }
    public void setSalesStartAt(LocalDateTime salesStartAt) { this.salesStartAt = salesStartAt; }
    public LocalDateTime getSalesEndAt() { return salesEndAt; }
    public void setSalesEndAt(LocalDateTime salesEndAt) { this.salesEndAt = salesEndAt; }
    public Long getVenueId() { return venueId; }
    public void setVenueId(Long venueId) { this.venueId = venueId; }
    public String getVenueName() { return venueName; }
    public void setVenueName(String venueName) { this.venueName = venueName; }
    public String getVenueCity() { return venueCity; }
    public void setVenueCity(String venueCity) { this.venueCity = venueCity; }
    public Long getOrganizerId() { return organizerId; }
    public void setOrganizerId(Long organizerId) { this.organizerId = organizerId; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public List<EventPricing> getPricings() { return pricings; }
    public void setPricings(List<EventPricing> pricings) { this.pricings = pricings; }
}
