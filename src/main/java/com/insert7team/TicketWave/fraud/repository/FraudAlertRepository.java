package com.insert7team.TicketWave.fraud.repository;

import com.insert7team.TicketWave.fraud.entity.FraudAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface FraudAlertRepository extends JpaRepository<FraudAlert, Long> {
    List<FraudAlert> findByUserId(Long userId);
    List<FraudAlert> findByResolvedFalse();
    int countByUserIdAndCreatedAtAfter(Long userId, LocalDateTime since);
}
