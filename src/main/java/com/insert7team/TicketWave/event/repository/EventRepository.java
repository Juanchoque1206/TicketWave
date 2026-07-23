package com.insert7team.TicketWave.event.repository;

import com.insert7team.TicketWave.event.domain.EventCategory;
import com.insert7team.TicketWave.event.domain.EventStatus;
import com.insert7team.TicketWave.event.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    Page<Event> findByStatus(EventStatus status, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.status = :status " +
           "AND (:city IS NULL OR LOWER(e.venueCity) = LOWER(:city)) " +
           "AND (:artist IS NULL OR LOWER(e.artist) LIKE LOWER(CONCAT('%', :artist, '%'))) " +
           "AND (:venueId IS NULL OR e.venueId = :venueId) " +
           "AND (:dateFrom IS NULL OR e.eventDate >= :dateFrom) " +
           "AND (:dateTo IS NULL OR e.eventDate <= :dateTo) " +
           "AND (:category IS NULL OR e.category = :category)")
    Page<Event> searchEvents(
            @Param("status") EventStatus status,
            @Param("city") String city,
            @Param("artist") String artist,
            @Param("venueId") Long venueId,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            @Param("category") EventCategory category,
            Pageable pageable
    );

    List<Event> findByOrganizerId(Long organizerId);

    List<Event> findTop10ByStatusAndEventDateAfterOrderByEventDateAsc(EventStatus status, LocalDateTime now);
}
