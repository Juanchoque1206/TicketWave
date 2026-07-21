package com.insert7team.TicketWave.order.repository;

import com.insert7team.TicketWave.common.enums.OrderStatus;
import com.insert7team.TicketWave.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);
    Optional<Order> findByOrderNumber(String orderNumber);
    List<Order> findByStatusAndExpiresAtBefore(OrderStatus status, LocalDateTime time);
    Page<Order> findByUserId(Long userId, Pageable pageable);
}
