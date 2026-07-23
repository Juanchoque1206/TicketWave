package com.insert7team.TicketWave.event.repository;

import com.insert7team.TicketWave.ticket.domain.TicketType;
import com.insert7team.TicketWave.event.entity.EventPricing;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EventPricingRepository extends JpaRepository<EventPricing, Long> {
    List<EventPricing> findByEventId(Long eventId);
    Optional<EventPricing> findByEventIdAndSectionIdAndTicketType(Long eventId, Long sectionId, TicketType ticketType);
}
