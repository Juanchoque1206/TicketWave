package com.insert7team.TicketWave.promotion.repository;

import com.insert7team.TicketWave.common.enums.PromotionScope;
import com.insert7team.TicketWave.promotion.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    Optional<Promotion> findByCodeAndActiveTrue(String code);
    List<Promotion> findByScopeAndActiveTrue(PromotionScope scope);
    List<Promotion> findByVenueIdAndActiveTrue(Long venueId);
    List<Promotion> findByEventIdAndActiveTrue(Long eventId);
}
