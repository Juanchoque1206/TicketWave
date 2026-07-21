package com.insert7team.TicketWave.venue.repository;

import com.insert7team.TicketWave.venue.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findBySectionId(Long sectionId);
    Optional<Seat> findBySectionIdAndRowAndNumber(Long sectionId, String row, int number);
}
