package com.insert7team.TicketWave.payment.repository;

import com.insert7team.TicketWave.payment.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RefundRepository extends JpaRepository<Refund, Long> {
    List<Refund> findByPaymentId(Long paymentId);
}
