package com.insert7team.TicketWave.ticket.entity;

import com.insert7team.TicketWave.shared.domain.model.AggregateRoot;
import com.insert7team.TicketWave.ticket.domain.TicketStatus;
import com.insert7team.TicketWave.ticket.domain.TicketType;
import com.insert7team.TicketWave.shared.infrastructure.exception.BusinessRuleException;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tickets", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"event_id", "seat_id"})
})
public class Ticket extends AggregateRoot {

    @Column(nullable = false, unique = true, length = 50)
    private String ticketCode;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "seat_id")
    private Long seatId;

    @Column(name = "section_id", nullable = false)
    private Long sectionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketType ticketType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(length = 500)
    private String qrCodeData;

    @Column(length = 300)
    private String eventTitle;

    private LocalDateTime eventDate;

    @Column(length = 200)
    private String venueName;

    @Column(length = 500)
    private String venueAddress;

    @Column(length = 100)
    private String sectionName;

    @Column(length = 20)
    private String seatLabel;

    @Column(length = 200)
    private String holderName;

    // --- Domain behavior ---

    public static Ticket issue(Long eventId, Long userId, Long sectionId, Long seatId,
                               TicketType ticketType, BigDecimal price,
                               String eventTitle, LocalDateTime eventDate,
                               String venueName, String venueAddress,
                               String sectionName, String seatLabel, String holderName) {
        Ticket ticket = new Ticket();
        ticket.ticketCode = UUID.randomUUID().toString();
        ticket.eventId = eventId;
        ticket.userId = userId;
        ticket.sectionId = sectionId;
        ticket.seatId = seatId;
        ticket.ticketType = ticketType;
        ticket.status = TicketStatus.CONFIRMED;
        ticket.price = price;
        ticket.qrCodeData = "TW:" + ticket.ticketCode;
        ticket.eventTitle = eventTitle;
        ticket.eventDate = eventDate;
        ticket.venueName = venueName;
        ticket.venueAddress = venueAddress;
        ticket.sectionName = sectionName;
        ticket.seatLabel = seatLabel;
        ticket.holderName = holderName;
        return ticket;
    }

    public void cancel() {
        if (this.status == TicketStatus.CANCELLED || this.status == TicketStatus.USED) {
            throw new BusinessRuleException("Ticket cannot be cancelled in its current status");
        }
        this.status = TicketStatus.CANCELLED;
    }

    public void markUsed() {
        if (this.status != TicketStatus.CONFIRMED) {
            throw new BusinessRuleException("Ticket is not in a valid state for entry");
        }
        this.status = TicketStatus.USED;
    }

    // --- Getters and setters ---

    public String getTicketCode() { return ticketCode; }
    public void setTicketCode(String ticketCode) { this.ticketCode = ticketCode; }
    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getSeatId() { return seatId; }
    public void setSeatId(Long seatId) { this.seatId = seatId; }
    public Long getSectionId() { return sectionId; }
    public void setSectionId(Long sectionId) { this.sectionId = sectionId; }
    public TicketType getTicketType() { return ticketType; }
    public void setTicketType(TicketType ticketType) { this.ticketType = ticketType; }
    public TicketStatus getStatus() { return status; }
    public void setStatus(TicketStatus status) { this.status = status; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getQrCodeData() { return qrCodeData; }
    public void setQrCodeData(String qrCodeData) { this.qrCodeData = qrCodeData; }
    public String getEventTitle() { return eventTitle; }
    public void setEventTitle(String eventTitle) { this.eventTitle = eventTitle; }
    public LocalDateTime getEventDate() { return eventDate; }
    public void setEventDate(LocalDateTime eventDate) { this.eventDate = eventDate; }
    public String getVenueName() { return venueName; }
    public void setVenueName(String venueName) { this.venueName = venueName; }
    public String getVenueAddress() { return venueAddress; }
    public void setVenueAddress(String venueAddress) { this.venueAddress = venueAddress; }
    public String getSectionName() { return sectionName; }
    public void setSectionName(String sectionName) { this.sectionName = sectionName; }
    public String getSeatLabel() { return seatLabel; }
    public void setSeatLabel(String seatLabel) { this.seatLabel = seatLabel; }
    public String getHolderName() { return holderName; }
    public void setHolderName(String holderName) { this.holderName = holderName; }
}
