package com.insert7team.TicketWave.notification.repository;

import com.insert7team.TicketWave.notification.entity.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
    List<NotificationLog> findByUserId(Long userId);
    List<NotificationLog> findBySentFalse();
}
