package com.insert7team.TicketWave.payment.dto;

import com.insert7team.TicketWave.payment.domain.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RefundResponse(
        Long id,
        Long orderId,
        BigDecimal amount,
        String reason,
        PaymentStatus status,
        LocalDateTime processedAt
) {}
