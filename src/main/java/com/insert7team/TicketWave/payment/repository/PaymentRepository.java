package com.insert7team.TicketWave.payment.repository;

import com.insert7team.TicketWave.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(Long orderId);
    Optional<Payment> findByExternalPaymentId(String externalId);
}
