package com.insert7team.TicketWave.payment.dto;

import com.insert7team.TicketWave.payment.domain.PaymentMethod;
import jakarta.validation.constraints.NotNull;

public record PaymentRequest(
        @NotNull PaymentMethod paymentMethod,
        String cardToken,
        String returnUrl
) {}
