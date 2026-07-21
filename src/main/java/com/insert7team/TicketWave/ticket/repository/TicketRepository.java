package com.insert7team.TicketWave.ticket.repository;

import com.insert7team.TicketWave.common.enums.TicketStatus;
import com.insert7team.TicketWave.ticket.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByUserId(Long userId);
    List<Ticket> findByEventId(Long eventId);
    Optional<Ticket> findByTicketCode(String ticketCode);
    boolean existsByEventIdAndSeatId(Long eventId, Long seatId);
    int countByEventIdAndUserIdAndStatusNot(Long eventId, Long userId, TicketStatus status);
}
