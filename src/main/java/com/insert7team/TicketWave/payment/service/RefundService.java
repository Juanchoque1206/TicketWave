package com.insert7team.TicketWave.payment.service;

import com.insert7team.TicketWave.payment.dto.RefundRequest;
import com.insert7team.TicketWave.payment.dto.RefundResponse;
import java.util.List;

public interface RefundService {
    RefundResponse requestRefund(Long orderId, RefundRequest request);
    RefundResponse getRefundStatus(Long refundId);
    List<RefundResponse> getRefundsForOrder(Long orderId);
}
