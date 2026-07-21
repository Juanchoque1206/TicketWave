package com.insert7team.TicketWave.payment.dto;

import com.insert7team.TicketWave.common.enums.PaymentMethod;
import com.insert7team.TicketWave.common.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        Long id,
        Long orderId,
        BigDecimal amount,
        String currency,
        PaymentMethod paymentMethod,
        PaymentStatus status,
        String externalPaymentId,
        LocalDateTime paidAt
) {}
