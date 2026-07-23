package com.insert7team.TicketWave.order.dto;

import com.insert7team.TicketWave.order.domain.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        String orderNumber,
        OrderStatus status,
        BigDecimal subtotal,
        BigDecimal discountAmount,
        BigDecimal totalAmount,
        String currency,
        String promotionCode,
        LocalDateTime expiresAt,
        List<OrderItemResponse> items,
        LocalDateTime createdAt
) {}
