package com.insert7team.TicketWave.ticket.entity;

import com.insert7team.TicketWave.common.entity.BaseEntity;
import com.insert7team.TicketWave.common.enums.TicketStatus;
import com.insert7team.TicketWave.common.enums.TicketType;
import com.insert7team.TicketWave.event.entity.Event;
import com.insert7team.TicketWave.user.entity.User;
import com.insert7team.TicketWave.venue.entity.Seat;
import com.insert7team.TicketWave.venue.entity.Section;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "tickets", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"event_id", "seat_id"})
})
public class Ticket extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String ticketCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id")
    private Seat seat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

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

    public String getTicketCode() { return ticketCode; }
    public void setTicketCode(String ticketCode) { this.ticketCode = ticketCode; }
    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Seat getSeat() { return seat; }
    public void setSeat(Seat seat) { this.seat = seat; }
    public Section getSection() { return section; }
    public void setSection(Section section) { this.section = section; }
    public TicketType getTicketType() { return ticketType; }
    public void setTicketType(TicketType ticketType) { this.ticketType = ticketType; }
    public TicketStatus getStatus() { return status; }
    public void setStatus(TicketStatus status) { this.status = status; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getQrCodeData() { return qrCodeData; }
    public void setQrCodeData(String qrCodeData) { this.qrCodeData = qrCodeData; }
}
