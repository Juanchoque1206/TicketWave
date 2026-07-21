package com.insert7team.TicketWave.event.entity;

import com.insert7team.TicketWave.common.entity.BaseEntity;
import com.insert7team.TicketWave.common.enums.EventCategory;
import com.insert7team.TicketWave.common.enums.EventStatus;
import com.insert7team.TicketWave.user.entity.User;
import com.insert7team.TicketWave.venue.entity.Venue;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "events", indexes = {
        @Index(name = "idx_event_date", columnList = "eventDate"),
        @Index(name = "idx_event_status", columnList = "status")
})
public class Event extends BaseEntity {

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    @Column(length = 500)
    private String imageUrl;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventPricing> pricings = new ArrayList<>();

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
    public Venue getVenue() { return venue; }
    public void setVenue(Venue venue) { this.venue = venue; }
    public User getOrganizer() { return organizer; }
    public void setOrganizer(User organizer) { this.organizer = organizer; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public List<EventPricing> getPricings() { return pricings; }
    public void setPricings(List<EventPricing> pricings) { this.pricings = pricings; }
}
