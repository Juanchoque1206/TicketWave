package com.insert7team.TicketWave.venue.repository;

import com.insert7team.TicketWave.venue.entity.Venue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VenueRepository extends JpaRepository<Venue, Long> {
    List<Venue> findByCity(String city);
    List<Venue> findByNameContainingIgnoreCase(String name);
    Page<Venue> findByCityIgnoreCase(String city, Pageable pageable);
}
